package dev.ecommerce.order.service;

import dev.ecommerce.order.constant.OrderStatus;
import dev.ecommerce.order.entity.Order;
import dev.ecommerce.order.entity.OrderItem;
import dev.ecommerce.order.model.ReserveResult;
import dev.ecommerce.order.constant.ReserveStatus;
import dev.ecommerce.order.repository.OrderRepository;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.repository.ProductRepository;
import dev.ecommerce.userInfo.constant.UserItemType;
import dev.ecommerce.userInfo.entity.UserItem;
import dev.ecommerce.userInfo.entity.UserUsageInfo;
import dev.ecommerce.userInfo.service.UserItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class CheckoutService {

    private final UserItemService userItemService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<List> reserveScript;
    private final RedisScript<List> releaseScript;
    private final RedisScript<Integer> removeReserveScript;

    private static String stockKey(long pid) { return "stock:{" + pid + "}"; }
    private static String holdsKey(long pid) { return "holds:{" + pid + "}"; }
    private static String expKey(long pid)   { return "exp:{" + pid + "}"; }
    private static String expAll() { return "exp:all"; }

    @Transactional
    public Long placeOrder(Long userId) {
        UserUsageInfo userInfo = userItemService.findUserInfoByUserId(userId);

        List<UserItem> carts = userInfo.getCarts();
        if (carts.isEmpty()) {
            throw new IllegalArgumentException("No items found for user");
        }

        Order order = new Order(OrderStatus.PROCESSING, userInfo, Instant.now());

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (UserItem cart : carts) {
            if (cart.getType() != UserItemType.CART)
                continue;

            long pid = cart.getProduct().getId();
            int expectedQuantity = cart.getQuantity();

            String qtyStr = (String) stringRedisTemplate.opsForHash().get(holdsKey(pid), cart.getId());
            int held = (qtyStr == null) ? 0 : Integer.parseInt(qtyStr);

            if (held <= 0 || held != expectedQuantity) {
                throw new IllegalArgumentException("Reservation missing or quantity mismatched");
            }

            Product product = cart.getProduct();
            BigDecimal price = product.getSalePrice() == null ? product.getPrice() : product.getSalePrice();
            totalPrice = totalPrice.add(price);
            OrderItem orderItem = new OrderItem(order, cart.getProduct(), cart.getQuantity(), price);
            order.getOrderItems().add(orderItem);
        }

        if (order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("No items found for user");
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            int res = productRepository.decreaseStockIfEnough(orderItem.getProduct().getId(), orderItem.getQuantity());
            if (res <= 0) {
                throw new IllegalArgumentException("Cannot decrease stock as not enough");
            }
        }

        order.setTotal(totalPrice);
        Order savedOrder = orderRepository.save(order);

        for (UserItem cart : carts) {
            cleanReservation(cart.getProduct().getId(), cart.getId());
        }

        return savedOrder.getId();
    }

    private void cleanReservation(long pid, long cartId) {
        stringRedisTemplate.execute(
                removeReserveScript,
                List.of(holdsKey(pid), expKey(pid), expAll()),
                String.valueOf(cartId), String.valueOf(pid)
        );
    }

    @Transactional(readOnly = true)
    public ReserveStatus reserve(Long userId) {
        List<UserItem> carts = userItemService.findUserInfoByUserId(userId).getCarts();

        for (UserItem cart : carts) {
            if (cart.getType() != UserItemType.CART)
                continue;
            ReserveResult result = reserveWithCache(cart.getProduct().getId(), cart.getId(), cart.getQuantity());
            if (result.status() == ReserveStatus.NO_CACHE) {
                Product product = cart.getProduct();
                addProductQuantityCache(product.getId(), product.getQuantity());
                ReserveResult retry = reserveWithCache(product.getId(), cart.getId(), cart.getQuantity());
                return retry.status();
            }
            return result.status();
        }

        return ReserveStatus.EMPTY;
    }

    private void addProductQuantityCache(long productId, int quantity) {
        stringRedisTemplate.opsForValue().set(
                stockKey(productId),
                String.valueOf(quantity)
        );
    }

    private ReserveResult reserveWithCache(Long productId, long cartId, int requestQuantity) {
        long exp = System.currentTimeMillis() + Duration.ofMinutes(30).toMillis();

        List<String> keys = List.of(stockKey(productId), holdsKey(productId), expKey(productId), expAll());
        List<String> args = List.of(String.valueOf(cartId), String.valueOf(requestQuantity),
                String.valueOf(exp), String.valueOf(productId));

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) stringRedisTemplate.execute(reserveScript, keys, args.toArray());

        if (result.size() != 2)
            return new ReserveResult(ReserveStatus.BAD_REQUEST, -3);
        long status = ((Number) result.getFirst()).longValue();
        long value =  ((Number) result.get(1)).longValue();

        if ((int) status == 1) {
            return new ReserveResult(ReserveStatus.OK, value);
        } else {
            return switch ((int) value) {
                case -1 -> new ReserveResult(ReserveStatus.NOT_ENOUGH, -1);
                case -2 -> new ReserveResult(ReserveStatus.NO_CACHE, -2);
                default -> new ReserveResult(ReserveStatus.BAD_REQUEST, value);
            };
        }
    }

    @Scheduled(fixedDelayString = "30000")
    public void removeExpiredReserve() {
        int count = 0;
        while (count < 5) {
            @SuppressWarnings("unchecked")
            List<Object> result = (List<Object>) stringRedisTemplate.execute(
                    releaseScript,
                    List.of("exp:all"),
                    String.valueOf(System.currentTimeMillis()),
                    "200"
            );
            count++;
            long processed = result.isEmpty() ? 0 : ((Number) result.getFirst()).longValue();
            if (processed == 0)
                break;
        }
    }

}

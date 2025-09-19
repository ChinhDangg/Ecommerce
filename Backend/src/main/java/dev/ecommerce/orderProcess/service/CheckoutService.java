package dev.ecommerce.orderProcess.service;

import dev.ecommerce.exceptionHandler.PaymentFailException;
import dev.ecommerce.orderProcess.constant.OrderStatus;
import dev.ecommerce.orderProcess.constant.PaymentStatus;
import dev.ecommerce.orderProcess.entity.Order;
import dev.ecommerce.orderProcess.entity.OrderItem;
import dev.ecommerce.orderProcess.entity.Payment;
import dev.ecommerce.orderProcess.model.CheckoutDTO;
import dev.ecommerce.orderProcess.model.ReserveResult;
import dev.ecommerce.orderProcess.constant.ReserveStatus;
import dev.ecommerce.orderProcess.model.UserReservationInfo;
import dev.ecommerce.orderProcess.repository.OrderRepository;
import dev.ecommerce.orderProcess.repository.PaymentRepository;
import dev.ecommerce.product.DTO.ProductCartDTO;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.repository.ProductRepository;
import dev.ecommerce.product.service.ProductService;
import dev.ecommerce.userInfo.constant.UserItemType;
import dev.ecommerce.userInfo.entity.UserItem;
import dev.ecommerce.userInfo.entity.UserUsageInfo;
import dev.ecommerce.userInfo.repository.UserUsageInfoRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class CheckoutService {

    private final UserItemService userItemService;
    private final UserUsageInfoRepository userInfoRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
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

        if (!userInfo.hasAddress()) {
            throw new IllegalArgumentException("No user address found for user");
        }

        Instant now = Instant.now();
        Order order = new Order(OrderStatus.PROCESSING, userInfo, now, userInfo.getUserAddress());

        for (UserItem cart : carts) {
            if (cart.getType() != UserItemType.CART)
                continue;

            long pid = cart.getProduct().getId();
            int expectedQuantity = cart.getQuantity();

            String qtyStr = (String) stringRedisTemplate.opsForHash().get(holdsKey(pid), String.valueOf(cart.getId()));
            int held = (qtyStr == null) ? 0 : Integer.parseInt(qtyStr);

            if (held <= 0 || held != expectedQuantity) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.saveAndFlush(order);
                throw new IllegalArgumentException("Reservation missing or quantity mismatched");
            }

            Product product = cart.getProduct();
            BigDecimal price = ProductService.getLowestPrice(product.getSalePrice(), product.getPrice());
            OrderItem orderItem = new OrderItem(order, cart.getProduct(), cart.getQuantity(), price);
            order.getOrderItems().add(orderItem);
        }

        if (order.getOrderItems().isEmpty()) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.saveAndFlush(order);
            throw new IllegalArgumentException("No items found for user");
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            int res = productRepository.decreaseStockIfEnough(orderItem.getProduct().getId(), orderItem.getQuantity());
            if (res <= 0) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.saveAndFlush(order);
                throw new IllegalArgumentException("Cannot decrease stock as not enough");
            }
        }

        order.setTotal(ProductService.getPriceAfterTax(
                carts, UserItem::getQuantity,
                item -> item.getProduct().getSalePrice(),
                item -> item.getProduct().getPrice()
        ));
        Order savedOrder = orderRepository.save(order);
        if (!processOrderPayment(savedOrder)) {
            throw new PaymentFailException("Cannot process order");
        }

        if (userInfo.getFirstOrderAt() == null) {
            userInfo.setFirstOrderAt(now);
            userInfoRepository.save(userInfo);
        }

        for (UserItem cart : carts) {
            cleanReservation(cart.getProduct().getId(), cart.getId());
        }

        return savedOrder.getId();
    }

    // placeholder for payment in the future
    @Transactional
    public boolean processOrderPayment(Order order) {
        System.out.println("Total: " + order.getTotal());
        Payment payment = new Payment(order, "VISA", "Who");
        if (true) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setConfirmedAt(Instant.now());
        } else {
            order.setStatus(OrderStatus.UNPAID);
            payment.setFailedAt(Instant.now());
            payment.setStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
        }
        return paymentRepository.save(payment).getStatus().equals(PaymentStatus.SUCCESS);
    }

    private void cleanReservation(long pid, long cartId) {
        stringRedisTemplate.execute(
                removeReserveScript,
                List.of(holdsKey(pid), expKey(pid), expAll()),
                String.valueOf(cartId), String.valueOf(pid)
        );
    }

    @Transactional(readOnly = true)
    public CheckoutDTO getUserCheckoutDTO(Long userId) {
        var reserveInfo = getUserReservations(userId, false);
        if (reserveInfo == null)
            return null;

        ProductCartDTO cartDTO = userItemService.getUserCartInfo(userId, true, false, true);
        UserUsageInfo userInfo = userItemService.findUserInfoByUserId(userId);
        return new CheckoutDTO(
                userInfo.getDisplayName(),
                userInfo.getUserAddress(),
                cartDTO,
                reserveInfo
        );
    }

    // redis:
    // key{pid} - field - value - where
    // exp:{1} - cart id - epoch time - sorted sets
    // hold:{1} - cart id - quantity - hash tables
    @Transactional(readOnly = true)
    public List<UserReservationInfo> getUserReservations(Long userId, boolean getFirstOnly) {
        List<UserItem> carts = userItemService.findUserInfoByUserId(userId).getCarts();
        List<UserReservationInfo> userReservationInfoList = new ArrayList<>();
        for (UserItem cart : carts) {
            if (cart.getType() != UserItemType.CART)
                continue;

            int expectedQuantity = cart.getQuantity();
            String qtyStr = (String) stringRedisTemplate.opsForHash().get(holdsKey(cart.getProduct().getId()), String.valueOf(cart.getId()));
            int held = (qtyStr == null) ? 0 : Integer.parseInt(qtyStr);
            if (held <= 0 || held != expectedQuantity)
                return null;

            Double expiryScore = stringRedisTemplate.opsForZSet().score(expAll(), cart.getProduct().getId() + ":" + cart.getId());
            long expiryTime = expiryScore == null ? 0 : expiryScore.longValue();
            if (expiryTime <= 0) {
                return null;
            }

            Instant storedTime = Instant.ofEpochMilli(expiryTime);
            long minutesDiff = Duration.between(Instant.now(), storedTime).toMinutes();

            userReservationInfoList.add(new UserReservationInfo(cart.getProduct().getId(), held, minutesDiff));

            if (getFirstOnly)
                break;
        }
        return userReservationInfoList;
    }

    @Transactional(readOnly = true)
    public ReserveStatus reserve(Long userId, boolean extend) {
        List<UserReservationInfo> userReservations = getUserReservations(userId, true);
        if (!extend && userReservations != null) {
            return ReserveStatus.ONGOING;
        }

        // if trying to extend reservation while still have more than 5 minutes left
        final int MIN_MINUTE_TO_RE_RESERVE = 5;
        if (extend && userReservations != null && userReservations.getFirst().minuteLeft() > MIN_MINUTE_TO_RE_RESERVE) {
            return ReserveStatus.BAD_REQUEST;
        }

        List<UserItem> carts = userItemService.findUserInfoByUserId(userId).getCarts();

        for (UserItem cart : carts) {
            if (cart.getType() != UserItemType.CART)
                continue;
            Product product = cart.getProduct();
            ReserveResult result = reserveWithCache(product.getId(), cart.getId(), cart.getQuantity());
            if (result.status() == ReserveStatus.NO_CACHE) {
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

package dev.ecommerce.checkout;

import dev.ecommerce.product.entity.Product;
import dev.ecommerce.user.constant.UserItemType;
import dev.ecommerce.user.entity.UserItem;
import dev.ecommerce.user.repository.UserItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class CheckoutService {

    private final UserItemRepository userItemRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<List> reserveScript;
    private final RedisScript<List> releaseScript;

    private static String stockKey(long pid) { return "stock:{" + pid + "}"; }
    private static String holdsKey(long pid) { return "holds:{" + pid + "}"; }
    private static String expKey(long pid)   { return "exp:{" + pid + "}"; }
    private static String expAll() { return "exp:all"; }

    @Transactional(readOnly = true)
    public ReserveStatus reserve(Long userId) {
        List<UserItem> carts = userItemRepository.getUserItemsByUserId(userId);

        for (UserItem cart : carts) {
            if (cart.getType() == UserItemType.SAVED)
                continue;
            ReserveResult result = reserveWithCache(cart.getProduct().getId(), cart.getId(), cart.getQuantity());
            if (result.status() == ReserveStatus.NO_CACHE) {
                Product product = cart.getProduct();
                addProductQuantityCache(product.getId(), product.getQuantity());
                ReserveResult retry =  reserveWithCache(product.getId(), cart.getId(), cart.getQuantity());
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
            System.out.println("processed: " + processed);
            if (processed == 0)
                break;
        }
    }

}

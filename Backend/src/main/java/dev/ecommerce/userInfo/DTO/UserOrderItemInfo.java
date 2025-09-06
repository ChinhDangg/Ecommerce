package dev.ecommerce.userInfo.DTO;

import dev.ecommerce.order.constant.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserOrderItemInfo(
    Long productId,
    String thumbnail,
    String productName,
    BigDecimal price,
    OrderStatus status,
    LocalDate statusDate
) {
}

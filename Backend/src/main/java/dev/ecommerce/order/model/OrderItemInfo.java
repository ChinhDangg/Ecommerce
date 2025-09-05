package dev.ecommerce.order.model;

import dev.ecommerce.order.constant.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderItemInfo(
    Long productId,
    String thumbnail,
    String productName,
    BigDecimal price,
    OrderStatus status,
    LocalDate statusDate
) {
}

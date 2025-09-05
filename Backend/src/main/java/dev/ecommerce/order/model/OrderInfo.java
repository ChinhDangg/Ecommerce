package dev.ecommerce.order.model;

import dev.ecommerce.order.constant.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderInfo(
        LocalDate orderPlaced,
        BigDecimal total,
        Long orderId,
        OrderStatus orderStatus,
        LocalDate statusDate,
        List<OrderItemInfo> items
) {}

package dev.ecommerce.userInfo.DTO;

import dev.ecommerce.orderProcess.constant.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UserOrderInfo(
        LocalDate orderPlaced,
        BigDecimal total,
        Long orderId,
        OrderStatus orderStatus,
        LocalDate statusDate,
        List<UserOrderItemInfo> items
) {}

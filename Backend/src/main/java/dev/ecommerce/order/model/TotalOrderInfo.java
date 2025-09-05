package dev.ecommerce.order.model;

import java.util.List;

public record TotalOrderInfo(
        long numbOfOrder,
        List<OrderInfo> orders
) {
}

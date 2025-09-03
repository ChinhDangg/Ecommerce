package dev.ecommerce.order.model;

import dev.ecommerce.order.constant.ReserveStatus;

public record ReserveResult(
        ReserveStatus status,
        long remaining
) {
}

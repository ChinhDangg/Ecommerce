package dev.ecommerce.orderProcess.model;

import dev.ecommerce.orderProcess.constant.ReserveStatus;

public record ReserveResult(
        ReserveStatus status,
        long remaining
) {
}

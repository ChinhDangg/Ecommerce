package dev.ecommerce.checkout;

public record ReserveResult(
        ReserveStatus status,
        long remaining
) {
}

package dev.ecommerce.product.DTO;

public record ProductReviewDTO(
        String userDisplayName,
        Integer rating,
        String title,
        String comment,
        String mediaURL
) {
}

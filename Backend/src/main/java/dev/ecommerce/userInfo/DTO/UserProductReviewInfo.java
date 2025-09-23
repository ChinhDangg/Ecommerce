package dev.ecommerce.userInfo.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserProductReviewInfo(
        Long productId,
        String thumbnail,
        String productName,
        String comment,
        String reviewMediaURL,
        @Valid @NotNull
        Integer rating,
        @Valid @NotBlank
        String reviewTitle
) {
}

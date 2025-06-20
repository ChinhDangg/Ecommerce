package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductDeleteDTO(
        Integer productLineId,
        @NotNull(message = "list of product id cannot be null")
        List<Long> productIdList
) {
}

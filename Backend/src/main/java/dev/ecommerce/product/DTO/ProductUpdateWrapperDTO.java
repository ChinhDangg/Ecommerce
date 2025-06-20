package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductUpdateWrapperDTO(
        ProductLineDTO productLineDTO,
        @NotNull(message = "Product List info cannot be null to update")
        List<ProductDTO> productDTOList) {
}


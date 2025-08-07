package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductWrapperDTO(
        ProductLineDTO productLineDTO,
        @NotNull(message = "Product List info cannot be null")
        List<ProductDTO> productDTOList) {
}


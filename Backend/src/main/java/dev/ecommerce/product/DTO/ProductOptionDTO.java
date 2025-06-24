package dev.ecommerce.product.DTO;

public record ProductOptionDTO(
        Long productId,
        String name,
        String valueOption
) {}
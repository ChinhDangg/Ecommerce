package dev.ecommerce.product.DTO;

public record ProductOptionDTO(
        Long productId,
        Integer id,
        String name,
        String valueOption
) {}
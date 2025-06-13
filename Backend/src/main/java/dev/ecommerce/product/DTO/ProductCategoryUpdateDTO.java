package dev.ecommerce.product.DTO;

import java.util.List;

public record ProductCategoryUpdateDTO(
        List<Long> productIds,
        Integer categoryId
) {}

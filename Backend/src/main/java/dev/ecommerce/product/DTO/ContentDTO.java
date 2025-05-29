package dev.ecommerce.product.DTO;

import dev.ecommerce.product.entity.ContentType;

public record ContentDTO(
        Long id,
        ContentType contentType,
        String content) {}

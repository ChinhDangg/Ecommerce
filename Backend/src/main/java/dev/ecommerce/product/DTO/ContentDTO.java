package dev.ecommerce.product.DTO;

import dev.ecommerce.product.constant.ContentType;

public record ContentDTO(
        Long id,
        ContentType contentType,
        String content) {}

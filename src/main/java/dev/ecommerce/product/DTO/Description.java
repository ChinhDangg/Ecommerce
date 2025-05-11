package dev.ecommerce.product.DTO;

import dev.ecommerce.product.entity.DescriptionType;

public record Description(DescriptionType type, String content) {}

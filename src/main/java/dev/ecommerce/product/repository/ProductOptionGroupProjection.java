package dev.ecommerce.product.repository;

import java.util.List;

public interface ProductOptionGroupProjection {
    String getName();
    List<String> getValueOptions();
}

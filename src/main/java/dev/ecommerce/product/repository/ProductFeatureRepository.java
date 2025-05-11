package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFeatureRepository extends JpaRepository<ProductFeature, Long> {
}

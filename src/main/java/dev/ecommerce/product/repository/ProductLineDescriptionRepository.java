package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductLineDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLineDescriptionRepository extends JpaRepository<ProductLineDescription, Long> {
}

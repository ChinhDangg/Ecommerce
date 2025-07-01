package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductCoreSpecification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCoreSpecificationRepository extends JpaRepository<ProductCoreSpecification, Integer> {
}

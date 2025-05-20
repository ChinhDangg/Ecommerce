package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {
}

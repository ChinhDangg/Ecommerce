package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLineRepository extends JpaRepository<ProductLine, Integer> {


}

package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "select id from products where product_line_id = :id", nativeQuery = true)
    Optional<Long[]> findAllIdByProductLineId(Integer id);

    Page<Product> findByCategoryId(Integer id, Pageable pageable);
}

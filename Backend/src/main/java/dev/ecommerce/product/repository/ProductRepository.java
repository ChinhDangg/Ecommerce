package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "select id from product where product_line_id = :id", nativeQuery = true)
    Optional<Long[]> findAllIdByProductLineId(Integer id);

    Page<Product> findByCategoryId(Integer id, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity - :amount " +
            "WHERE p.id = :productId AND p.quantity >= :amount")
    int decreaseStockIfEnough(@Param("productId") Long productId,
                              @Param("amount") int amount);
}

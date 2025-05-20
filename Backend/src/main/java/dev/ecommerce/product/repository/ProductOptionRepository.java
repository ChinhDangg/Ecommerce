package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    @Query(value = "SELECT po.name AS name, array_agg(po.value_option) AS value_options " +
            "FROM product_options po " +
            "WHERE po.product_line_id = :productLineId " +
            "GROUP BY po.name", nativeQuery = true)
    List<ProductOptionGroupProjection> findProductOptionByProductLine(@Param("productLineId") Integer productLineId);
}

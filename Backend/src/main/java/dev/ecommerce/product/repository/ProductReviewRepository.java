package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Optional<ProductReview> findByProductIdAndUserInfoId(Long productId, Long userId);
}

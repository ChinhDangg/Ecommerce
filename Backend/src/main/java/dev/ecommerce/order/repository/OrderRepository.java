package dev.ecommerce.order.repository;

import dev.ecommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdAndPlacedAtGreaterThanEqualAndPlacedAtLessThanOrderByPlacedAtDesc(Long id, Instant start, Instant end,  Pageable pageable);

    long countAllByUserIdAndPlacedAtGreaterThanEqualAndPlacedAtLessThanOrderByPlacedAtDesc(Long id, Instant start, Instant end);

    @Query("SELECT MIN(o.placedAt) FROM Order o WHERE o.user.id = :userId")
    Instant findOldestPlacedAtByUserId(@Param("userId") Long userId);
}

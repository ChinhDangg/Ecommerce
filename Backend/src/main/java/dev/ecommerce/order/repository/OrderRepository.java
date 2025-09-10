package dev.ecommerce.order.repository;

import dev.ecommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserInfoIdAndPlacedAtGreaterThanEqualAndPlacedAtLessThanOrderByPlacedAtDesc(Long id, Instant start, Instant end,  Pageable pageable);

    long countAllByUserInfoIdAndPlacedAtGreaterThanEqualAndPlacedAtLessThanOrderByPlacedAtDesc(Long id, Instant start, Instant end);
}

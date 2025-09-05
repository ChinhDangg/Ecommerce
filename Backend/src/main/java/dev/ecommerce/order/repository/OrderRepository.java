package dev.ecommerce.order.repository;

import dev.ecommerce.order.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserIdOrderByPlacedAtDesc(Long id, Instant cutOff, Pageable pageable);

    long countAllByUserIdOrderByPlacedAtDesc(Long id, Instant cutOff);
}

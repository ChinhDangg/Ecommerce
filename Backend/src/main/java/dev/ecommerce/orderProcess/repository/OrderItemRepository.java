package dev.ecommerce.orderProcess.repository;

import dev.ecommerce.orderProcess.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Optional<OrderItem> findFirstByOrderUserInfoIdAndProductId(Long orderUserInfoId, Long productId);
}

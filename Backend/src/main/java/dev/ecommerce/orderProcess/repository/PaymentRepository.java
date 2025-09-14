package dev.ecommerce.orderProcess.repository;

import dev.ecommerce.orderProcess.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

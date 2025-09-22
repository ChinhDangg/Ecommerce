package dev.ecommerce.orderProcess.entity;

import dev.ecommerce.orderProcess.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String method;

    @Column(name = "provider_txn_id", length = 100) // apply unique in the future for processing
    private String providerTxnId;

    private final Instant createdAt = Instant.now();
    @Setter
    private Instant confirmedAt;
    @Setter
    private Instant failedAt;

    public Payment(Order order, String method, String providerTxnId) {
        this.order = order;
        this.method = method;
        this.providerTxnId = providerTxnId;
    }

}

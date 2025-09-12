package dev.ecommerce.order.entity;

import dev.ecommerce.order.constant.OrderStatus;
import dev.ecommerce.userInfo.entity.UserUsageInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_info_id", nullable = false)
    private UserUsageInfo userInfo;

    @OneToMany(cascade =  CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();

    private Instant placedAt;

    @Setter
    private BigDecimal total;

    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Setter
    private Instant statusTime; // for showing info for status like Delivered date or refunded date.

    public Order(OrderStatus status, UserUsageInfo userInfo, Instant placedAt) {
        this.status = status;
        this.userInfo = userInfo;
        this.placedAt = placedAt;
        statusTime = placedAt;
    }
}

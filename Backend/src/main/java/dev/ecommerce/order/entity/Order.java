package dev.ecommerce.order.entity;

import dev.ecommerce.order.constant.OrderStatus;
import dev.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(cascade =  CascadeType.ALL)
    private final List<OrderItem> orderItems = new ArrayList<>();

    private Instant placedAt;

    public Order(OrderStatus status, User user, Instant placedAt) {
        this.status = status;
        this.user = user;
        this.placedAt = placedAt;
    }
}

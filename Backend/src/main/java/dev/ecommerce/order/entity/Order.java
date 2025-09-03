package dev.ecommerce.order.entity;

import dev.ecommerce.order.constant.OrderStatus;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OrderStatus status;

    private int quantity;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

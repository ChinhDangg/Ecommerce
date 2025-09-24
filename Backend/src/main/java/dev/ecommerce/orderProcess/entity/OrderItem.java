package dev.ecommerce.orderProcess.entity;

import dev.ecommerce.orderProcess.constant.OrderStatus;
import dev.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_item")
@NoArgsConstructor
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    // price paid at the time of order
    private BigDecimal unitPrice;

    // individual status for each item in-case one item in the group order get returned or cancelled
    @Setter
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Setter
    private Instant statusTime;

    public OrderItem(Order order, Product product, int quantity, BigDecimal unitPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

}

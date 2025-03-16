package dev.ecommerce.product.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product_prices")
public class ProductPrice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(precision = 10, scale = 2)  // 8 digits before, 2 after decimal
    private BigDecimal price;

    private Integer discount;

    private LocalDate endDisCountDate;
}

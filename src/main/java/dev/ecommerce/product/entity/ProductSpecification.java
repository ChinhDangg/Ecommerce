package dev.ecommerce.product.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_specifications")
public class ProductSpecification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "product_id")
    private Product product;

    private String name;

    private String value;
}

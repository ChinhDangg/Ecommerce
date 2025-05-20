package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_specifications")
public class ProductSpecification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String name;

    private String valueOption;

    public ProductSpecification(Product product, String name, String valueOption) {
        this.product = product;
        this.name = name;
        this.valueOption = valueOption;
    }
}

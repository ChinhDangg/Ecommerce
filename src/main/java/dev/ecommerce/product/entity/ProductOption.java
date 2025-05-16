package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_options")
public class ProductOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String name;

    private String valueOption;

    public ProductOption(Product product, String name, String valueOption) {
        this.product = product;
        this.name = name;
        this.valueOption = valueOption;
    }
}

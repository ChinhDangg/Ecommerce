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

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;

    private String name;

    private String valueOption;

    public ProductOption(Product product, ProductLine productLine, String name, String valueOption) {
        this.product = product;
        this.productLine = productLine;
        this.name = name;
        this.valueOption = valueOption;
    }
}

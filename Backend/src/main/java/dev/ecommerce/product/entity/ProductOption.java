package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_options")
public class ProductOption extends BaseOption {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;

    public ProductOption(Product product, ProductLine productLine, String name, String valueOption) {
        this.product = product;
        this.productLine = productLine;
        this.name = name;
        this.valueOption = valueOption;
    }
}

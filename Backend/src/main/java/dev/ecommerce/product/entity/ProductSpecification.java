package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_specifications")
public class ProductSpecification extends BaseOption {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductSpecification(Product product, String name, String valueOption) {
        this.product = product;
        this.name = name;
        this.valueOption = valueOption;
    }
}

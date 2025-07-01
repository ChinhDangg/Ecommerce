package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_specifications")
public class ProductSpecification extends BaseOption {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "product_filter_id")
    private ProductCoreSpecification productCoreSpecification;

    public ProductSpecification(Product product, ProductCoreSpecification productCoreSpecification, String name, String valueOption) {
        this.product = product;
        this.productCoreSpecification = productCoreSpecification;
        this.name = name;
        this.valueOption = valueOption;
    }
}
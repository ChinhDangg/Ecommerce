package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product_core_spec_option")
@NoArgsConstructor
public class ProductCoreSpecOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "product_core_spec_id")
    private ProductCoreSpecification productCoreSpecification;

    private String option;

    public ProductCoreSpecOption(String option, ProductCoreSpecification productCoreSpecification) {
        this.productCoreSpecification = productCoreSpecification;
        this.option = option;
    }
}

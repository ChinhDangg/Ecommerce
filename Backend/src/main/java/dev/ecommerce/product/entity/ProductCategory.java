package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_categories")
public class ProductCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ProductCategory parentProductCategory;

    @OneToMany(mappedBy = "parentProductCategory", cascade = CascadeType.PERSIST)
    private final List<ProductCategory> subcategories = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private final List<ProductCoreSpecification> coreSpecs = new ArrayList<>();

    public ProductCategory(String name, ProductCategory parentProductCategory) {
        this.name = name;
        this.parentProductCategory = parentProductCategory;
    }
}

package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product_categories")
public class ProductCategory {
    @Setter(AccessLevel.NONE)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ProductCategory parentProductCategory;

    @OneToMany(mappedBy = "parentProductCategory", cascade = CascadeType.PERSIST)
    private List<ProductCategory> subcategories = new ArrayList<>();
}

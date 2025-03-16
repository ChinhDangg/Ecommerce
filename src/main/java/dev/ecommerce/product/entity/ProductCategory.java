package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Table(name = "product_categories")
public class ProductCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ProductCategory parentProductCategory;

    @OneToMany(mappedBy = "parentProductCategory", cascade = CascadeType.PERSIST)
    private List<ProductCategory> subcategories = new ArrayList<>();
}

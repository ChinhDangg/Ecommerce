package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_lines")
public class ProductLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "productLine")
    @OrderBy("sortOrder ASC")
    private final List<ProductLineMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "productLine")
    @OrderBy("sortOrder ASC")
    private final List<ProductLineDescription> descriptions = new ArrayList<>();

    // variants of product (configurations or options) fall into same product line
    //@JsonManagedReference
    @OneToMany(mappedBy = "productLine")
    private final List<Product> products = new ArrayList<>();

    public ProductLine(String name) {
        this.name = name;
    }

}

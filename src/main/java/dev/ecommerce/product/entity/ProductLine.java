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
    private final List<ProductLineMedia> productLineMedias = new ArrayList<>();

    // variants of product (configurations or options) fall into same product line
    @OneToMany(mappedBy = "productLine")
    private final List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "productLine")
    private final List<ProductLineDescription> productLineDescriptions = new ArrayList<>();

    public ProductLine(String name) {
        this.name = name;
    }

}

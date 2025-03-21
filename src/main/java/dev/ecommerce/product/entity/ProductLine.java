package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_lines")
public class ProductLine {
    @Setter(AccessLevel.NONE)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    // variants of product (configurations or options) fall into same product line
    @OneToMany(mappedBy = "productLine")
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "productLine")
    private List<ProductDescription> productLineDescriptions = new ArrayList<>();

}

package dev.ecommerce.product.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_lines")
public class ProductLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    // variants of product (configurations or options) fall into same product line
    @OneToMany(mappedBy = "productLine")
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "productLine")
    private List<ProductDescription> productLineDescriptions = new ArrayList<>();

}

package dev.ecommerce.product.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;

    @OneToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    private Integer manufacturerId;

    private String name;

    private String brand;

    private Integer quantity;

    private Integer totalRatings;

    private Integer totalReviews;

    @Enumerated(EnumType.STRING)
    private ConditionType condition;

    private LocalDate addedDate;

    @OneToOne(mappedBy = "product")
    private ProductPrice price;

    @OneToMany(mappedBy = "product")
    private List<ProductDescription> descriptions = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductFeature> features = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductSpecification> specifications = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductReview> reviews = new ArrayList<>();
}

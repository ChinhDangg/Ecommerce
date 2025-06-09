package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "products")
public class Product {
    @Setter(AccessLevel.NONE)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String manufacturerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType conditionType;

    @Column(nullable = false)
    private LocalDate addedDate;

    @Column(precision = 10, scale = 2, nullable = false)  // 8 digits before, 2 after decimal
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal salePrice;

    private LocalDate saleEndDate;

    //@JsonBackReference
    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    public Product(String manufacturerId, String name, String brand,
                   Integer quantity, ConditionType conditionType, LocalDate addedDate,
                   BigDecimal price, BigDecimal salePrice, LocalDate saleEndDate,
                   ProductLine productLine, ProductCategory category) {
        this.manufacturerId = manufacturerId;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.conditionType = conditionType;
        this.addedDate = addedDate;
        this.price = price;
        this.salePrice = salePrice;
        this.saleEndDate = saleEndDate;
        this.productLine = productLine;
        this.category = category;
    }

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductOption> options = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductSpecification> specifications = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductFeature> features = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private final List<ProductMedia> media = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private final List<ProductDescription> descriptions = new ArrayList<>();

    private Integer totalRatings;

    private Integer totalReviews;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductReview> reviews = new ArrayList<>();

    public Product(Long id, String manufacturerId, String name,
                   Integer quantity, BigDecimal price, BigDecimal salePrice, LocalDate saleEndDate,
                   ProductCategory category, ProductLine productLine, ProductMedia media) {
        this.id = id;
        this.manufacturerId = manufacturerId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.salePrice = salePrice;
        this.saleEndDate = saleEndDate;
        this.category = category;
        this.productLine = productLine;
        this.media.add(media);
    }
}

package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Setter;

@Entity
@Table(name = "product_descriptions")
public class ProductDescription {
    @Setter(AccessLevel.NONE)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // can have either description for one product or the whole product line
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // product exclusive description

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;  // product-shared descriptions

    @Enumerated(EnumType.STRING)
    private DescriptionType descriptionType; // TEXT, IMAGE, VIDEO

    // text or image_url or video_url
    private String content;

    // to know which description to display first
    private Integer sortOrder;

    // for images and video alt text
    private String altText;
}

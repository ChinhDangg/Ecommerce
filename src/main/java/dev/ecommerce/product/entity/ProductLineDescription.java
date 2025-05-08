package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_line_descriptions")
public class ProductLineDescription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;  // product-shared descriptions

    @Enumerated(EnumType.STRING)
    private DescriptionType descriptionType; // TEXT, IMAGE, VIDEO

    // text or image_url or video_url
    private String content;

    // to know which description to display first
    private Integer sortOrder;

    public ProductLineDescription(ProductLine productLine, DescriptionType descriptionType,
                                  String content, Integer sortOrder) {
        this.productLine = productLine;
        this.descriptionType = descriptionType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

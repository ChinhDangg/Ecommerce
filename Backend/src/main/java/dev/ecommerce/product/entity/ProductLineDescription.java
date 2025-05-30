package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    @Enumerated(EnumType.STRING)
    private ContentType contentType; // TEXT, IMAGE, VIDEO

    // text or image_url or video_url
    @Setter
    private String content;

    // to know which description to display first
    @Setter
    private Integer sortOrder;

    public ProductLineDescription(ProductLine productLine, ContentType contentType,
                                  String content, Integer sortOrder) {
        this.productLine = productLine;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

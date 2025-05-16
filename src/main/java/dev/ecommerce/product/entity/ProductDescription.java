package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_descriptions")
public class ProductDescription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // can have either description for one product or the whole product line
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // product exclusive description

    @Enumerated(EnumType.STRING)
    private ContentType contentType; // TEXT, IMAGE, VIDEO

    // text or image_url or video_url
    private String content;

    // to know which description to display first
    private Integer sortOrder;

    public ProductDescription(Product product, ContentType contentType, String content, Integer sortOrder) {
        this.product = product;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

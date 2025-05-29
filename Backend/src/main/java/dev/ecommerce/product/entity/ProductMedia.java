package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "product_media")
public class ProductMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // can have either media for one product or the whole product line
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // product exclusive media

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private String content;

    private Integer sortOrder;

    public ProductMedia(Product product, ContentType contentType, String content, Integer sortOrder) {
        this.product = product;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

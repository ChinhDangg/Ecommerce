package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "product_media")
public class ProductMedia extends BaseContent {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductMedia(Product product, ContentType contentType, String content, Integer sortOrder) {
        this.product = product;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

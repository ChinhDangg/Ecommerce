package dev.ecommerce.product.entity;

import dev.ecommerce.product.constant.ContentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_description")
public class ProductDescription extends BaseContent {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductDescription(Product product, ContentType contentType, String content, Integer sortOrder) {
        this.product = product;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

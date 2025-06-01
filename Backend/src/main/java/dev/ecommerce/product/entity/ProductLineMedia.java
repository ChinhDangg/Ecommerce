package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "product_line_media")
public class ProductLineMedia extends BaseContent {

    @ManyToOne
    @JoinColumn(name = "product_line_id", nullable = false)
    private ProductLine productLine;  // product-shared media

    public ProductLineMedia(ProductLine productLine, ContentType contentType, String content, Integer sortOrder) {
        this.productLine = productLine;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

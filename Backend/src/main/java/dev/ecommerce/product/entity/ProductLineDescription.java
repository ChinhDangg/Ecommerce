package dev.ecommerce.product.entity;

import dev.ecommerce.product.constant.ContentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_line_description")
public class ProductLineDescription extends BaseContent {

    @ManyToOne
    @JoinColumn(name = "product_line_id", nullable = false)
    private ProductLine productLine;  // product-shared descriptions

    public ProductLineDescription(ProductLine productLine, ContentType contentType,
                                  String content, Integer sortOrder) {
        this.productLine = productLine;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

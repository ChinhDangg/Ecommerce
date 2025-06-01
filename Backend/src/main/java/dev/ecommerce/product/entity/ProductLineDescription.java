package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_line_descriptions")
public class ProductLineDescription extends BaseContent {

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;  // product-shared descriptions

    public ProductLineDescription(ProductLine productLine, ContentType contentType,
                                  String content, Integer sortOrder) {
        this.productLine = productLine;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

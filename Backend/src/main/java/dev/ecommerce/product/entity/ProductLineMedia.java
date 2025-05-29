package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "product_line_media")
public class ProductLineMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;  // product-shared media

    @Setter
    @Enumerated(EnumType.STRING)
    private ContentType contentType; // IMAGE, VIDEO

    @Setter
    private String content;

    @Setter
    private Integer sortOrder;

    public ProductLineMedia(ProductLine productLine, ContentType contentType, String content, Integer sortOrder) {
        this.productLine = productLine;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}

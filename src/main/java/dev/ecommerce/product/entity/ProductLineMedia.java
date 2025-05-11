package dev.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String contentURL;

    private Integer sortOrder;

    public ProductLineMedia(ProductLine productLine, String contentURL, Integer sortOrder) {
        this.productLine = productLine;
        this.contentURL = contentURL;
        this.sortOrder = sortOrder;
    }
}

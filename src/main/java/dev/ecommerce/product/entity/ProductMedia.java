package dev.ecommerce.product.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_media")
public class ProductMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // can have either media for one product or the whole product line
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // product exclusive media

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;  // product-shared media

    private String contentURL;

    private Integer sortOrder;
}

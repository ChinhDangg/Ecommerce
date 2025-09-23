package dev.ecommerce.product.entity;

import dev.ecommerce.userInfo.entity.UserUsageInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "product_review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "user_info_id"})
        }
)
public class ProductReview {
    @Setter(AccessLevel.NONE)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_info_id", nullable = false)
    private UserUsageInfo userInfo;

    @Setter
    private String title;

    @Setter
    private String comment;

    @Setter
    @Column(name = "media_url")
    private String mediaURL;

    @Setter
    private Integer rating;

    public ProductReview(Product product, UserUsageInfo userInfo,
                         String title, String comment, Integer rating, String mediaURL) {
        this.product = product;
        this.userInfo = userInfo;
        this.title = title;
        this.comment = comment;
        this.rating = rating;
        this.mediaURL = mediaURL;
    }
}

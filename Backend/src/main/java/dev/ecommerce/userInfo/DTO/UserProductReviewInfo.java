package dev.ecommerce.userInfo.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProductReviewInfo {

    private Long productId;
    private String thumbnail;
    private String productName;
    private String comment;
    private String reviewMediaURL;
    @Valid @NotNull
    private Integer rating;
    @Valid @NotBlank
    private String reviewTitle;

    public UserProductReviewInfo(Long productId, String thumbnail, String productName) {
        this.productId = productId;
        this.thumbnail = thumbnail;
        this.productName = productName;
    }
}

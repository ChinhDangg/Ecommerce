package dev.ecommerce.product.DTO;

import dev.ecommerce.user.constant.UserItemType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ShortProductCartDTO {

    private final Long id;

    private final String manufacturerId;

    private final String name;

    private final BigDecimal price;

    @Setter
    private List<ProductOptionDTO> productOptions;

    @Setter
    private String imageName;

    @Setter
    private BigDecimal discountedPrice;

    @Setter
    private boolean newRelease;

    @Setter
    private UserItemType itemType;

    @Setter
    private Integer quantity;

    @Setter
    private Integer maxQuantity;

    public ShortProductCartDTO(Long id, String manufacturerId, String name, BigDecimal price) {
        this.id = id;
        this.manufacturerId = manufacturerId;
        this.name = name;
        this.price = price;
    }
}

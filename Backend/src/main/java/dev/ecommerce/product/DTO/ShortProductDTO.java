package dev.ecommerce.product.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ShortProductDTO {

    private final Integer productLineId;

    private final Long id;

    private final String manufacturerId;

    private final String name;

    private final Integer quantity;

    private final BigDecimal price;

    private final List<String> features;

    @Setter
    private String imageName;

    @Setter
    private BigDecimal discountedPrice;

    @Setter
    private boolean newRelease;

    public ShortProductDTO(Integer productLineId, Long id, String manufacturerId, String name,
                           Integer quantity, BigDecimal price, List<String> features) {
        this.productLineId = productLineId;
        this.id = id;
        this.manufacturerId = manufacturerId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.features = features;
    }
}

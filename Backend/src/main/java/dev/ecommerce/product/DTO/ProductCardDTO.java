package dev.ecommerce.product.DTO;

import dev.ecommerce.product.constant.ConditionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ProductCardDTO {
    private final Integer productLineId;
    private final Long id;
    private final String manufacturerId;
    private final String name;
    private final String brand;
    private final Integer quantity;
    private final ConditionType conditionType;

    @Setter
    private List<ProductOptionDTO> productGroupedOptions;

    @Setter
    private List<ProductCategoryDTO> productCategoryChain;

    private final BigDecimal price;
    private final BigDecimal salePrice;
    private final LocalDate saleEndDate;
    private final List<OptionDTO> options;
    private final List<OptionDTO> specifications;
    private final List<String> features;
    private final List<ContentDTO> media;
    private final List<ContentDTO> descriptions;
}

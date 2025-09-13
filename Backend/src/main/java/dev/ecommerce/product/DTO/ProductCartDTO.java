package dev.ecommerce.product.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCartDTO {
    private List<ShortProductCartDTO> productList;
    private int totalQuantity;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
    private BigDecimal priceAfterTax;
}

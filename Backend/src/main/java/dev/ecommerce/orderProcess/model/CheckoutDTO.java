package dev.ecommerce.orderProcess.model;

import dev.ecommerce.product.DTO.ProductCartDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class CheckoutDTO {
    private String displayName;
    private String address;
    private ProductCartDTO productInfo;
    private Map<Long, Map<String, Long>> reserveInfo;
}

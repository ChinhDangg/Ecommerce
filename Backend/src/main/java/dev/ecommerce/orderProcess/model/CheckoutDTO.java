package dev.ecommerce.orderProcess.model;

import dev.ecommerce.product.DTO.ProductCartDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CheckoutDTO {
    private String displayName;
    private String address;
    private ProductCartDTO productInfo;
    private List<UserReservationInfo> reserveInfo;
}

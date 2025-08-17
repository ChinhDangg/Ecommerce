package dev.ecommerce.user.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCartDTO {
    Long productId;
    Integer quantity;
}

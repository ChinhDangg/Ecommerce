package dev.ecommerce.userInfo.DTO;

import dev.ecommerce.userInfo.constant.UserItemType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCartDTO {
    @NotNull
    Long productId;
    @NotNull
    Integer quantity;
    UserItemType itemType;
}

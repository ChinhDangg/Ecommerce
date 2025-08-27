package dev.ecommerce.user.DTO;

import dev.ecommerce.user.constant.UserItemType;
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

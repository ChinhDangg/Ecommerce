package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductInformationDTO {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String productLineName;

}

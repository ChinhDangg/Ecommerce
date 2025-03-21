package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@Setter
public class ProductCategoryDTO {
    @Min(value = 1, message = "Category value must be positive")
    private Integer id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 2 and 30 characters")
    private String name;
}

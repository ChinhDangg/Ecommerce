package dev.ecommerce.product.DTO;

import dev.ecommerce.product.entity.ConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class ProductDTO {

    private Integer productLineId;

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotNull(message = "Manufacturer id is required")
    private String manufacturerId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Product condition is required")
    private ConditionType conditionType;

    @NotNull(message = "Product category is required")
    private Integer categoryId;

    @NotBlank(message = "Price is required")
    private String regularPrice;
    private String salePrice;
    private LocalDate saleEndDate;

    private Option[] options;

    private Specification[] specifications;

    @Size(max = 10, message = "Max of 10 features only")
    private String[] features;

    @Size(max = 5, message = "Max of 5 images only")
    private String[] imageNames;

    @Size(max = 10, message = "Max of 10 descriptions only")
    private Description[] descriptions;
}

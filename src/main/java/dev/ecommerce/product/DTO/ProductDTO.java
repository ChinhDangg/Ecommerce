package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class ProductDTO {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    private String brand;
    private Integer manufacturerId;
    private Integer quantity;

    private String regularPrice;
    private String salePrice;
    private LocalDate saleEndDate;

    @NotNull(message = "category cannot be null")
    private String categoryId;

    private String[] options;

    private String[] specs;

    @Size(max = 10, message = "Max of 10 features only")
    private String[] features;

    @Size(max = 5, message = "Max of 5 images only")
    private String[] imageNames;

    @Size(max = 10, message = "Max of 10 descriptions only")
    private Descriptions[] descriptions;
}

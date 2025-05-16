package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProductLineDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Size(max = 5, message = "Max of 5 media only")
    private List<ContentDTO> media;

    @Size(max = 10, message = "Max of 10 descriptions only")
    private List<ContentDTO> descriptions;
}

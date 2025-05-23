package dev.ecommerce.product.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class ProductLineDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private final String name;

    @Size(max = 5, message = "Max of 5 media only")
    private final List<ContentDTO> media;

    @Size(max = 10, message = "Max of 10 descriptions only")
    private final List<ContentDTO> descriptions;

    @Setter
    private Long[] productIdList;

    public ProductLineDTO(String name, List<ContentDTO> media, List<ContentDTO> descriptions) {
        this.name = name;
        this.media = media;
        this.descriptions = descriptions;
    }
}

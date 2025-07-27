package dev.ecommerce.product.DTO;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record ProductSearchResultDTO(
        Map<String, List<Map<String, Object>>> specialFilters,
        Map<String, List<Map<String, Object>>> specFilters,
        Page<ShortProductDTO> productResults
) {
}

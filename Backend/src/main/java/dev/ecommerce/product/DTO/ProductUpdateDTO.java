package dev.ecommerce.product.DTO;

import java.util.List;

public record ProductUpdateDTO(
    ProductLineDTO productLineDTO,
    List<ProductDTO> updatingProductDTOList,
    List<ProductDTO> newProductDTOList
    ) {}

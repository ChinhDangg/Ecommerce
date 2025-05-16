package dev.ecommerce.product.DTO;

import dev.ecommerce.product.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductLineDTO toDTO(ProductLine productLine);

    //List<ProductLineDTO> toDTOList(List<ProductLine> productLines);

    ContentDTO descriptionsToContentDTO(ProductLineDescription productLineDescription);

    ContentDTO mediaToContentDTO(ProductLineMedia productLineMedia);

    List<ProductCategoryDTO> toDTOList(List<ProductCategory> productCategory);

    @Mapping(source = "productLine.id", target = "productLineId")
    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTO(List<Product> products);
}

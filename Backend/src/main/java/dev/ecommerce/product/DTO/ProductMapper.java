package dev.ecommerce.product.DTO;

import dev.ecommerce.product.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductLineDTO toProductLineDTO(ProductLine productLine);

    ContentDTO descriptionsToContentDTO(ProductLineDescription productLineDescription);

    ContentDTO mediaToContentDTO(ProductLineMedia productLineMedia);

    ProductCategoryDTO toProductCategoryDTO(ProductCategory productCategory);

    List<ProductCategoryDTO> toProductCategoryDTOList(List<ProductCategory> productCategory);

    @Mapping(source = "product.id", target = "productId")
    ProductOptionDTO toProductOptionDTO(ProductOption productOption);

    List<ProductOptionDTO> toProductOptionDTOList(List<ProductOption> productOption);

    @Mapping(source = "productLine.id", target = "productLineId")
    @Mapping(source = "features", target = "features", qualifiedByName = "featuresToContentList")
    ProductCardDTO toProductCardDTO(Product product);

    @Mapping(source = "productLine.id", target = "productLineId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "features", target = "features", qualifiedByName = "featuresToContentList")
    ProductDTO toProductDTO(Product product);

    @Mapping(source = "features", target = "features", qualifiedByName = "featuresToContentList")
    ShortProductDTO toShortProductWithFeaturesDTO(Product product);

    @Mapping(target = "features", ignore = true)
    ShortProductDTO toShortProductWithoutFeaturesDTO(Product product);

    @Named("featuresToContentList")
    default List<String> mapFeaturesToContentList(List<ProductFeature> features) {
        if (features == null) {
            return null;
        }
        return features.stream()
                .map(ProductFeature::getContent)
                .filter(Objects::nonNull) // Handle null content
                .toList();
    }

}

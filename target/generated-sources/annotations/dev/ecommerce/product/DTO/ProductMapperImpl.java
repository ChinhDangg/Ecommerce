package dev.ecommerce.product.DTO;

import dev.ecommerce.product.constant.ConditionType;
import dev.ecommerce.product.constant.ContentType;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.entity.ProductCategory;
import dev.ecommerce.product.entity.ProductDescription;
import dev.ecommerce.product.entity.ProductLine;
import dev.ecommerce.product.entity.ProductLineDescription;
import dev.ecommerce.product.entity.ProductLineMedia;
import dev.ecommerce.product.entity.ProductMedia;
import dev.ecommerce.product.entity.ProductOption;
import dev.ecommerce.product.entity.ProductSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-09T23:58:05-0400",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductLineDTO toProductLineDTO(ProductLine productLine) {
        if ( productLine == null ) {
            return null;
        }

        List<ContentDTO> media = null;
        List<ContentDTO> descriptions = null;
        Integer id = null;
        String name = null;

        media = productLineMediaListToContentDTOList( productLine.getMedia() );
        descriptions = productLineDescriptionListToContentDTOList( productLine.getDescriptions() );
        id = productLine.getId();
        name = productLine.getName();

        ProductLineDTO productLineDTO = new ProductLineDTO( id, name, media, descriptions );

        return productLineDTO;
    }

    @Override
    public ContentDTO descriptionsToContentDTO(ProductLineDescription productLineDescription) {
        if ( productLineDescription == null ) {
            return null;
        }

        Long id = null;
        ContentType contentType = null;
        String content = null;

        id = productLineDescription.getId();
        contentType = productLineDescription.getContentType();
        content = productLineDescription.getContent();

        ContentDTO contentDTO = new ContentDTO( id, contentType, content );

        return contentDTO;
    }

    @Override
    public ContentDTO mediaToContentDTO(ProductLineMedia productLineMedia) {
        if ( productLineMedia == null ) {
            return null;
        }

        Long id = null;
        ContentType contentType = null;
        String content = null;

        id = productLineMedia.getId();
        contentType = productLineMedia.getContentType();
        content = productLineMedia.getContent();

        ContentDTO contentDTO = new ContentDTO( id, contentType, content );

        return contentDTO;
    }

    @Override
    public ProductCategoryDTO toProductCategoryDTO(ProductCategory productCategory) {
        if ( productCategory == null ) {
            return null;
        }

        Integer id = null;
        String name = null;

        id = productCategory.getId();
        name = productCategory.getName();

        ProductCategoryDTO productCategoryDTO = new ProductCategoryDTO( id, name );

        return productCategoryDTO;
    }

    @Override
    public List<ProductCategoryDTO> toProductCategoryDTOList(List<ProductCategory> productCategory) {
        if ( productCategory == null ) {
            return null;
        }

        List<ProductCategoryDTO> list = new ArrayList<ProductCategoryDTO>( productCategory.size() );
        for ( ProductCategory productCategory1 : productCategory ) {
            list.add( toProductCategoryDTO( productCategory1 ) );
        }

        return list;
    }

    @Override
    public ProductOptionDTO toProductOptionDTO(ProductOption productOption) {
        if ( productOption == null ) {
            return null;
        }

        Long productId = null;
        Integer id = null;
        String name = null;
        String valueOption = null;

        productId = productOptionProductId( productOption );
        if ( productOption.getId() != null ) {
            id = productOption.getId().intValue();
        }
        name = productOption.getName();
        valueOption = productOption.getValueOption();

        ProductOptionDTO productOptionDTO = new ProductOptionDTO( productId, id, name, valueOption );

        return productOptionDTO;
    }

    @Override
    public List<ProductOptionDTO> toProductOptionDTOList(List<ProductOption> productOption) {
        if ( productOption == null ) {
            return null;
        }

        List<ProductOptionDTO> list = new ArrayList<ProductOptionDTO>( productOption.size() );
        for ( ProductOption productOption1 : productOption ) {
            list.add( toProductOptionDTO( productOption1 ) );
        }

        return list;
    }

    @Override
    public ShortProductCartDTO toShortProductCartDTO(ShortProductDTO shortProductDTO) {
        if ( shortProductDTO == null ) {
            return null;
        }

        Long id = null;
        String manufacturerId = null;
        String name = null;
        BigDecimal price = null;

        id = shortProductDTO.getId();
        manufacturerId = shortProductDTO.getManufacturerId();
        name = shortProductDTO.getName();
        price = shortProductDTO.getPrice();

        ShortProductCartDTO shortProductCartDTO = new ShortProductCartDTO( id, manufacturerId, name, price );

        shortProductCartDTO.setImageName( shortProductDTO.getImageName() );
        shortProductCartDTO.setDiscountedPrice( shortProductDTO.getDiscountedPrice() );
        shortProductCartDTO.setNewRelease( shortProductDTO.isNewRelease() );
        shortProductCartDTO.setQuantity( shortProductDTO.getQuantity() );

        return shortProductCartDTO;
    }

    @Override
    public ProductCardDTO toProductCardDTO(Product product) {
        if ( product == null ) {
            return null;
        }

        Integer productLineId = null;
        List<String> features = null;
        List<OptionDTO> options = null;
        List<OptionDTO> specifications = null;
        List<ContentDTO> media = null;
        List<ContentDTO> descriptions = null;
        Long id = null;
        String manufacturerId = null;
        String name = null;
        String brand = null;
        Integer quantity = null;
        ConditionType conditionType = null;
        BigDecimal price = null;
        BigDecimal salePrice = null;
        LocalDate saleEndDate = null;

        productLineId = productProductLineId( product );
        features = mapFeaturesToContentList( product.getFeatures() );
        options = productOptionListToOptionDTOList( product.getOptions() );
        specifications = productSpecificationListToOptionDTOList( product.getSpecifications() );
        media = productMediaListToContentDTOList( product.getMedia() );
        descriptions = productDescriptionListToContentDTOList( product.getDescriptions() );
        id = product.getId();
        manufacturerId = product.getManufacturerId();
        name = product.getName();
        brand = product.getBrand();
        quantity = product.getQuantity();
        conditionType = product.getConditionType();
        price = product.getPrice();
        salePrice = product.getSalePrice();
        saleEndDate = product.getSaleEndDate();

        ProductCardDTO productCardDTO = new ProductCardDTO( productLineId, id, manufacturerId, name, brand, quantity, conditionType, price, salePrice, saleEndDate, options, specifications, features, media, descriptions );

        return productCardDTO;
    }

    @Override
    public ProductDTO toProductDTO(Product product) {
        if ( product == null ) {
            return null;
        }

        Integer productLineId = null;
        Integer categoryId = null;
        List<String> features = null;
        List<OptionDTO> options = null;
        List<OptionDTO> specifications = null;
        List<ContentDTO> media = null;
        List<ContentDTO> descriptions = null;
        Long id = null;
        String manufacturerId = null;
        String name = null;
        String brand = null;
        Integer quantity = null;
        ConditionType conditionType = null;
        BigDecimal price = null;
        BigDecimal salePrice = null;
        LocalDate saleEndDate = null;

        productLineId = productProductLineId( product );
        categoryId = productCategoryId( product );
        features = mapFeaturesToContentList( product.getFeatures() );
        options = productOptionListToOptionDTOList( product.getOptions() );
        specifications = productSpecificationListToOptionDTOList( product.getSpecifications() );
        media = productMediaListToContentDTOList( product.getMedia() );
        descriptions = productDescriptionListToContentDTOList( product.getDescriptions() );
        id = product.getId();
        manufacturerId = product.getManufacturerId();
        name = product.getName();
        brand = product.getBrand();
        quantity = product.getQuantity();
        conditionType = product.getConditionType();
        price = product.getPrice();
        salePrice = product.getSalePrice();
        saleEndDate = product.getSaleEndDate();

        ProductDTO productDTO = new ProductDTO( productLineId, id, manufacturerId, name, brand, quantity, conditionType, categoryId, price, salePrice, saleEndDate, options, specifications, features, media, descriptions );

        return productDTO;
    }

    @Override
    public ShortProductDTO toShortProductWithFeaturesDTO(Product product) {
        if ( product == null ) {
            return null;
        }

        List<String> features = null;
        Long id = null;
        String manufacturerId = null;
        String name = null;
        Integer quantity = null;
        BigDecimal price = null;

        features = mapFeaturesToContentList( product.getFeatures() );
        id = product.getId();
        manufacturerId = product.getManufacturerId();
        name = product.getName();
        quantity = product.getQuantity();
        price = product.getPrice();

        ShortProductDTO shortProductDTO = new ShortProductDTO( id, manufacturerId, name, quantity, price, features );

        return shortProductDTO;
    }

    @Override
    public ShortProductDTO toShortProductWithoutFeaturesDTO(Product product) {
        if ( product == null ) {
            return null;
        }

        Long id = null;
        String manufacturerId = null;
        String name = null;
        Integer quantity = null;
        BigDecimal price = null;

        id = product.getId();
        manufacturerId = product.getManufacturerId();
        name = product.getName();
        quantity = product.getQuantity();
        price = product.getPrice();

        List<String> features = null;

        ShortProductDTO shortProductDTO = new ShortProductDTO( id, manufacturerId, name, quantity, price, features );

        return shortProductDTO;
    }

    protected List<ContentDTO> productLineMediaListToContentDTOList(List<ProductLineMedia> list) {
        if ( list == null ) {
            return null;
        }

        List<ContentDTO> list1 = new ArrayList<ContentDTO>( list.size() );
        for ( ProductLineMedia productLineMedia : list ) {
            list1.add( mediaToContentDTO( productLineMedia ) );
        }

        return list1;
    }

    protected List<ContentDTO> productLineDescriptionListToContentDTOList(List<ProductLineDescription> list) {
        if ( list == null ) {
            return null;
        }

        List<ContentDTO> list1 = new ArrayList<ContentDTO>( list.size() );
        for ( ProductLineDescription productLineDescription : list ) {
            list1.add( descriptionsToContentDTO( productLineDescription ) );
        }

        return list1;
    }

    private Long productOptionProductId(ProductOption productOption) {
        Product product = productOption.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }

    private Integer productProductLineId(Product product) {
        ProductLine productLine = product.getProductLine();
        if ( productLine == null ) {
            return null;
        }
        return productLine.getId();
    }

    protected OptionDTO productOptionToOptionDTO(ProductOption productOption) {
        if ( productOption == null ) {
            return null;
        }

        Integer id = null;
        String name = null;
        String valueOption = null;

        if ( productOption.getId() != null ) {
            id = productOption.getId().intValue();
        }
        name = productOption.getName();
        valueOption = productOption.getValueOption();

        OptionDTO optionDTO = new OptionDTO( id, name, valueOption );

        return optionDTO;
    }

    protected List<OptionDTO> productOptionListToOptionDTOList(List<ProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<OptionDTO> list1 = new ArrayList<OptionDTO>( list.size() );
        for ( ProductOption productOption : list ) {
            list1.add( productOptionToOptionDTO( productOption ) );
        }

        return list1;
    }

    protected OptionDTO productSpecificationToOptionDTO(ProductSpecification productSpecification) {
        if ( productSpecification == null ) {
            return null;
        }

        Integer id = null;
        String name = null;
        String valueOption = null;

        if ( productSpecification.getId() != null ) {
            id = productSpecification.getId().intValue();
        }
        name = productSpecification.getName();
        valueOption = productSpecification.getValueOption();

        OptionDTO optionDTO = new OptionDTO( id, name, valueOption );

        return optionDTO;
    }

    protected List<OptionDTO> productSpecificationListToOptionDTOList(List<ProductSpecification> list) {
        if ( list == null ) {
            return null;
        }

        List<OptionDTO> list1 = new ArrayList<OptionDTO>( list.size() );
        for ( ProductSpecification productSpecification : list ) {
            list1.add( productSpecificationToOptionDTO( productSpecification ) );
        }

        return list1;
    }

    protected ContentDTO productMediaToContentDTO(ProductMedia productMedia) {
        if ( productMedia == null ) {
            return null;
        }

        Long id = null;
        ContentType contentType = null;
        String content = null;

        id = productMedia.getId();
        contentType = productMedia.getContentType();
        content = productMedia.getContent();

        ContentDTO contentDTO = new ContentDTO( id, contentType, content );

        return contentDTO;
    }

    protected List<ContentDTO> productMediaListToContentDTOList(List<ProductMedia> list) {
        if ( list == null ) {
            return null;
        }

        List<ContentDTO> list1 = new ArrayList<ContentDTO>( list.size() );
        for ( ProductMedia productMedia : list ) {
            list1.add( productMediaToContentDTO( productMedia ) );
        }

        return list1;
    }

    protected ContentDTO productDescriptionToContentDTO(ProductDescription productDescription) {
        if ( productDescription == null ) {
            return null;
        }

        Long id = null;
        ContentType contentType = null;
        String content = null;

        id = productDescription.getId();
        contentType = productDescription.getContentType();
        content = productDescription.getContent();

        ContentDTO contentDTO = new ContentDTO( id, contentType, content );

        return contentDTO;
    }

    protected List<ContentDTO> productDescriptionListToContentDTOList(List<ProductDescription> list) {
        if ( list == null ) {
            return null;
        }

        List<ContentDTO> list1 = new ArrayList<ContentDTO>( list.size() );
        for ( ProductDescription productDescription : list ) {
            list1.add( productDescriptionToContentDTO( productDescription ) );
        }

        return list1;
    }

    private Integer productCategoryId(Product product) {
        ProductCategory category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getId();
    }
}

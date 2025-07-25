package dev.ecommerce.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.*;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductLineRepository productLineRepository;
    private final ProductRepository productRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final ProductMediaRepository productMediaRepository;
    private final ProductDescriptionRepository productDescriptionRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductSpecificationRepository productSpecificationRepository;
    private final ProductCoreSpecificationRepository productCoreSpecificationRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductCategoryRepository productCategoryRepository,
            ProductLineRepository productLineRepository,
            ProductRepository productRepository,
            ProductFeatureRepository productFeatureRepository,
            ProductMediaRepository productMediaRepository,
            ProductDescriptionRepository productDescriptionRepository,
            ProductOptionRepository productOptionRepository,
            ProductSpecificationRepository productSpecificationRepository,
            ProductCoreSpecificationRepository productCoreSpecificationRepository,
            ProductMapper productMapper
    ) {
        this.productCategoryRepository = productCategoryRepository;
        this.productLineRepository = productLineRepository;
        this.productRepository = productRepository;
        this.productFeatureRepository = productFeatureRepository;
        this.productMediaRepository = productMediaRepository;
        this.productDescriptionRepository = productDescriptionRepository;
        this.productOptionRepository = productOptionRepository;
        this.productSpecificationRepository = productSpecificationRepository;
        this.productCoreSpecificationRepository = productCoreSpecificationRepository;
        this.productMapper = productMapper;
    }

    public Product getProductById(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Product id is null");
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> getProductCategoryChain(Long id) {
        Product product = getProductById(id);
        List<ProductCategoryDTO> productCategoryChain = new ArrayList<>();
        ProductCategory parentCategory = product.getCategory();
        while (parentCategory != null) {
            productCategoryChain.add(productMapper.toProductCategoryDTO(parentCategory));
            parentCategory = parentCategory.getParentProductCategory();
        }
        return productCategoryChain.reversed();
    }

    @Transactional
    public Page<ShortProductDTO> findProductsByCategory(Integer id, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Product> productPage = productRepository.findByCategoryId(id, pageable);
        return productPage.map(productMapper::toShortProductWithFeaturesDTO);
    }

    @Transactional(readOnly = true)
    public ProductDTO findProductById(Long id) {
        Product foundProduct = getProductById(id);
        foundProduct.getProductLine();
        foundProduct.getCategory().getId();
        foundProduct.getOptions().size();
        foundProduct.getSpecifications().size();
        foundProduct.getFeatures().size();
        foundProduct.getMedia().size();
        foundProduct.getDescriptions().size();
        return productMapper.toProductDTO(foundProduct);
    }

    @Transactional
    public Long saveProduct(ProductDTO productDTO) {
        if (productDTO.getName() == null)
            throw new DataIntegrityViolationException("Product name is null");

        ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        ProductLine productLine = productDTO.getProductLineId() == null
                ? null : productLineRepository.findById(productDTO.getProductLineId())
                .orElseThrow(() -> new IllegalArgumentException("Product line not found"));
        Product savedProduct = productRepository.save(new Product(
                productDTO.getManufacturerId(),
                productDTO.getName(),
                productDTO.getBrand(),
                productDTO.getQuantity(),
                productDTO.getConditionType(),
                LocalDate.now(),
                productDTO.getPrice(),
                productDTO.getSalePrice(),
                productDTO.getSaleEndDate(),
                productLine,
                category
        ));

        List<ProductFeature> featureList = new ArrayList<>();
        for(String featureName : productDTO.getFeatures()) {
            featureList.add(new ProductFeature(savedProduct, featureName));
        }
        if (!featureList.isEmpty())
            productFeatureRepository.saveAll(featureList);

        List<ProductMedia> mediaList = new ArrayList<>();
        for (int j = 0; j < productDTO.getMedia().size(); j++) {
            mediaList.add(new ProductMedia(
                    savedProduct,
                    productDTO.getMedia().get(j).contentType(),
                    productDTO.getMedia().get(j).content(),
                    j
            ));
        }
        if (!mediaList.isEmpty())
            productMediaRepository.saveAll(mediaList);

        List<ProductDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productDTO.getDescriptions().size(); j++) {
            descriptionList.add(new ProductDescription(
                    savedProduct,
                    productDTO.getDescriptions().get(j).contentType(),
                    productDTO.getDescriptions().get(j).content(),
                    j
            ));
        }
        if (!descriptionList.isEmpty())
            productDescriptionRepository.saveAll(descriptionList);

        List<ProductOption> optionList = new ArrayList<>();
        for (OptionDTO option : productDTO.getOptions()) {
            optionList.add(new ProductOption(savedProduct, productLine, option.name(), option.valueOption()));
        }
        if (!optionList.isEmpty())
            productOptionRepository.saveAll(optionList);

        List<ProductSpecification> specificationList = new ArrayList<>();
        for (OptionDTO specification : productDTO.getSpecifications()) {
            specificationList.add(new ProductSpecification(
                    savedProduct,
                    specification.id() == null ? null : productCoreSpecificationRepository.findById(specification.id())
                            .orElseThrow(() -> new IllegalArgumentException("Predefined Specification not found")),
                    specification.name(),
                    specification.valueOption()
            ));
        }
        if (!specificationList.isEmpty())
            productSpecificationRepository.saveAll(specificationList);

        return savedProduct.getId();
    }

    @Transactional
    public Long updateProductInfo(ProductDTO productDTO) {
        Product product = getProductById(productDTO.getId());

        // update basic info
        if (!productDTO.getManufacturerId().equals(product.getManufacturerId()))
            product.setManufacturerId(productDTO.getManufacturerId());
        if (!productDTO.getName().equals(product.getName()))
            product.setName(productDTO.getName());
        if (!productDTO.getBrand().equals(product.getBrand()))
            product.setBrand(productDTO.getBrand());
        if (!productDTO.getQuantity().equals(product.getQuantity()))
            product.setQuantity(productDTO.getQuantity());
        if (!productDTO.getConditionType().equals(product.getConditionType()))
            product.setConditionType(productDTO.getConditionType());
        if (!productDTO.getPrice().equals(product.getPrice()))
            product.setPrice(productDTO.getPrice());
        if (!productDTO.getSalePrice().equals(product.getSalePrice()))
            product.setSalePrice(productDTO.getSalePrice());
        if (!productDTO.getSaleEndDate().equals(product.getSaleEndDate()))
            product.setSaleEndDate(productDTO.getSaleEndDate());
        if (!product.getCategory().getId().equals(productDTO.getCategoryId())) {
            ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product category not found"));
            product.setCategory(category);
        }

        // updating features
        List<ProductFeature> currentFeatures = product.getFeatures();
        Map<String, ProductFeature> currentFeatureMap = currentFeatures.stream()
                .collect(Collectors.toMap(ProductFeature::getContent, f -> f));
        List<ProductFeature> updatedFeatures = new ArrayList<>();
        // Add or reuse features
        for (String value : productDTO.getFeatures()) {
            ProductFeature feature = currentFeatureMap.remove(value); // remove existing ones
            if (feature == null)
                feature = new ProductFeature(product, value); // New feature
            updatedFeatures.add(feature);
        }
        product.getFeatures().clear(); // should not take effect until flush
        product.getFeatures().addAll(updatedFeatures); // cancels deletion of same context features if use get features

        // update media
        Map<Long, ProductMedia> currentMediaMap = product.getMedia().stream() // to get existing media entity by id quickly
                .collect(Collectors.toMap(ProductMedia::getId, m -> m));
        List<ProductMedia> updatedMediaList = buildUpdatedMediaList(
                productDTO.getMedia(),
                currentMediaMap,
                (dto, sortOrder) -> new ProductMedia(product, dto.contentType(), dto.content(), sortOrder)
        );
        product.getMedia().clear();
        product.getMedia().addAll(updatedMediaList);

        // update description
        Map<Long, ProductDescription> currentDescriptionMap = product.getDescriptions().stream() // to get existing media entity by id quickly
                .collect(Collectors.toMap(ProductDescription::getId, d -> d));
        List<ProductDescription> updatedDescriptionList = buildUpdatedMediaList(
                productDTO.getDescriptions(),
                currentDescriptionMap,
                (dto, sortOrder) -> new ProductDescription(product, dto.contentType(), dto.content(), sortOrder)
        );
        product.getDescriptions().clear();
        product.getDescriptions().addAll(updatedDescriptionList);

        // update option
        ProductLine productLine = product.getProductLine();

        Map<String, ProductOption> currentOptionMap = product.getOptions().stream()
                .collect(Collectors.toMap(ProductOption::getName, o -> o));
        List<ProductOption> updatedOptionList = buildUpdateOptionList(
                productDTO.getOptions(),
                currentOptionMap,
                (dto) -> new ProductOption(product, productLine, dto.name(), dto.valueOption())
        );
        product.getOptions().clear();
        product.getOptions().addAll(updatedOptionList);

        Map<String, ProductSpecification> currentSpecificationMap = product.getSpecifications().stream()
                .collect(Collectors.toMap(ProductSpecification::getName, s -> s));
        List<ProductSpecification> updatedSpecificationList = buildUpdateOptionList(
                productDTO.getSpecifications(),
                currentSpecificationMap,
                (dto) -> new ProductSpecification(
                        product,
                        dto.id() == null ? null : productCoreSpecificationRepository.findById(dto.id())
                                .orElseThrow(() -> new IllegalArgumentException("Predefined Specification not found")),
                        dto.name(),
                        dto.valueOption()
                )
        );
        product.getSpecifications().clear();
        product.getSpecifications().addAll(updatedSpecificationList);

        return productRepository.save(product).getId();
    }

    private <T extends BaseOption> List<T> buildUpdateOptionList(
            List<OptionDTO> incomingDTOs,
            Map<String, T> currentOptionMap,
            Function<OptionDTO, T> newOptionFactory
    ) {
        List<T> updatedOptionList = new ArrayList<>();
        for (OptionDTO dto : incomingDTOs) {
            if (currentOptionMap.containsKey(dto.name())) {
                T option = currentOptionMap.get(dto.name());
                if (!dto.valueOption().equals(option.getName()))
                    option.setOption(dto.valueOption());
                updatedOptionList.add(option);
            } else {
                T newOption = newOptionFactory.apply(dto);
                updatedOptionList.add(newOption);
            }
        }
        return updatedOptionList;
    }

    public static <T extends BaseContent> List<T> buildUpdatedMediaList(
            List<ContentDTO> incomingDTOs,
            Map<Long, T> currentMediaMap,
            BiFunction<ContentDTO, Integer, T> newMediaFactory
    ) {
        List<T> updatedList = new ArrayList<>();
        int order = 0;
        for (ContentDTO dto : incomingDTOs) {
            if (dto.id() != null && currentMediaMap.containsKey(dto.id())) {
                T media = currentMediaMap.get(dto.id());
                if (dto.contentType() != media.getContentType())
                    media.setContentType(dto.contentType());
                if (!dto.content().equals(media.getContent()))
                    media.setContent(dto.content());
                if (media.getSortOrder() != order)
                    media.setSortOrder(order);
                updatedList.add(media);
            } else {
                T newMedia = newMediaFactory.apply(dto, order);
                updatedList.add(newMedia);
            }
            order++;
        }
        return updatedList;
    }

    @Transactional
    public void deleteProductById(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}

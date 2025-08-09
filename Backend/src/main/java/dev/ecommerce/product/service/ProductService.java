package dev.ecommerce.product.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.*;
import dev.ecommerce.product.constant.ContentType;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

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
    private final ProductLineService productLineService;
    private final MediaService mediaService;

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
            ProductMapper productMapper,
            ProductLineService productLineService,
            MediaService mediaService) {
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
        this.productLineService = productLineService;
        this.mediaService = mediaService;
    }

    public Product findProductById(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Product id is null");
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductDTOById(Long id) {
        Product foundProduct = findProductById(id);
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
    public Long saveProduct(ProductDTO productDTO, Integer productLineId, Map<String, MultipartFile> fileMap) {
        if (productDTO.getName() == null)
            throw new DataIntegrityViolationException("Product name is null");

        List<String> filenameList = new ArrayList<>();

        // unsaved media if transaction failed
        TransactionSynchronizationManager.registerSynchronization(mediaService.getMediaTransactionSyn(filenameList));

        ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        ProductLine productLine = productLineId != null ? productLineService.findProductLineById(productLineId)
                : productDTO.getProductLineId() == null ? null : productLineService.findProductLineById(productDTO.getProductLineId());
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
        productFeatureRepository.saveAll(featureList);

        List<ProductMedia> mediaList = new ArrayList<>();
        for (int j = 0; j < productDTO.getMedia().size(); j++) {
            ContentDTO mediaDTO = productDTO.getMedia().get(j);

            String mediaName = mediaService.checkAndSaveMediaFile(mediaDTO, fileMap.get(mediaDTO.content()));
            filenameList.add(mediaName);

            mediaList.add(new ProductMedia(savedProduct, mediaDTO.contentType(), mediaDTO.content(), j));
        }
        productMediaRepository.saveAll(mediaList);

        List<ProductDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productDTO.getDescriptions().size(); j++) {
            ContentDTO descriptionDTO = productDTO.getDescriptions().get(j);

            String contentName = mediaService.checkAndSaveMediaFile(descriptionDTO, fileMap.get(descriptionDTO.content()));
            if (descriptionDTO.contentType().isMedia()) {
                filenameList.add(contentName);
            }

            descriptionList.add(new ProductDescription(savedProduct, descriptionDTO.contentType(), contentName, j));
        }
        productDescriptionRepository.saveAll(descriptionList);

        List<ProductOption> optionList = new ArrayList<>();
        for (OptionDTO option : productDTO.getOptions()) {
            optionList.add(new ProductOption(savedProduct, productLine, option.name(), option.valueOption()));
        }
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
        productSpecificationRepository.saveAll(specificationList);

        return savedProduct.getId();
    }

    @Transactional
    public Long updateProductInfo(ProductDTO productDTO) {
        Product product = findProductById(productDTO.getId());

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
                    option.setValueOption(dto.valueOption());
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
        Product product = findProductById(id);
        productRepository.delete(product);
    }
}

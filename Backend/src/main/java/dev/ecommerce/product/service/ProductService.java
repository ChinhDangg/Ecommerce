package dev.ecommerce.product.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.*;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import dev.ecommerce.userInfo.DTO.UserCartDTO;
import dev.ecommerce.userInfo.constant.UserItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductCategoryRepository productCategoryRepository;
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

    public void updateProductProductLine(Integer productLineId, Long productId) {
        Product product = findProductById(productId);
        ProductLine productLine = productLineService.findProductLineById(productLineId);
        product.setProductLine(productLine);
        productRepository.save(product);
        logger.info("Updated product line for product: {}", productId);
    }

    @Transactional(readOnly = true)
    public ProductCartDTO getLocalCartInfo(List<UserCartDTO> userCartDTOList) {
        List<ShortProductCartDTO> shortProductDTOList = getShortProductCartInfo(
                userCartDTOList,
                userCartDTO -> findProductById(userCartDTO.getProductId()),
                UserCartDTO::getQuantity,
                UserCartDTO::getItemType);
        return getProductCartInfo(shortProductDTOList, false);
    }

    @Transactional(readOnly = true)
    public <T> List<ShortProductCartDTO> getShortProductCartInfo(List<T> carts,
                                                          Function<T, Product> productGetter,
                                                          Function<T, Integer> quantityGetter,
                                                          Function<T, UserItemType> itemTypeGetter) {
        List<ShortProductCartDTO> shortProductCartDTOList = new ArrayList<>();
        for (T cart : carts) {
            Product product = productGetter.apply(cart);
            ShortProductCartDTO shortProductCartDTO = productMapper.toShortProductCartDTO(getShortProductInfo(product, false));
            shortProductCartDTO.setProductOptions(
                    productMapper.toProductOptionDTOList(product.getOptions())
            );
            shortProductCartDTO.setItemType(itemTypeGetter.apply(cart));
            int maxQuantity = product.getQuantity() > 100 ? 100 : product.getQuantity();
            int cartQuantity = quantityGetter.apply(cart);
            shortProductCartDTO.setMaxQuantity(maxQuantity);
            shortProductCartDTO.setQuantity(Math.min(cartQuantity, maxQuantity));
            shortProductCartDTOList.add(shortProductCartDTO);
        }
        return shortProductCartDTOList;
    }

    public ProductCartDTO getProductCartInfo(List<ShortProductCartDTO> productList, boolean getFinalTotal) {
        int totalQuantity = productList.stream()
                .map(ShortProductCartDTO::getQuantity)
                .reduce(0, Integer::sum);

        BigDecimal priceBeforeTax = getPriceBeforeTax(productList, ShortProductCartDTO::getQuantity,
                ShortProductCartDTO::getDiscountedPrice, ShortProductCartDTO::getPrice);
        BigDecimal priceAfterTax = getFinalTotal ? getPriceAfterTax(priceBeforeTax): null;
        BigDecimal taxedAmount = getFinalTotal ? priceAfterTax.subtract(priceBeforeTax).setScale(2, RoundingMode.HALF_UP) : null;

        return new ProductCartDTO(productList,
                totalQuantity,
                taxedAmount,
                priceBeforeTax,
                priceAfterTax);
    }

    public static BigDecimal getLowestPrice(BigDecimal salePrice, BigDecimal originalPrice) {
        if (salePrice == null)
            return originalPrice;
        if (salePrice.compareTo(originalPrice) > 0)
            throw new IllegalArgumentException("SalePrice cannot be greater than originalPrice");
        return salePrice;
    }

    // pair: price + quantity
    public static <T> BigDecimal getPriceBeforeTax(List<T> userItems,
                                                   Function<T, Integer> quantityGetter,
                                                   Function<T, BigDecimal> salePriceGetter,
                                                   Function<T, BigDecimal> priceGetter) {
        BigDecimal priceBeforeTax = BigDecimal.ZERO;
        for (T userItem : userItems) {
            BigDecimal price = getLowestPrice(salePriceGetter.apply(userItem), priceGetter.apply(userItem))
                    .multiply(BigDecimal.valueOf(quantityGetter.apply(userItem)));
            priceBeforeTax = priceBeforeTax.add(price);
        }
        return priceBeforeTax.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal getPriceAfterTax(BigDecimal priceBeforeTax) {
        BigDecimal priceAfterTax = priceBeforeTax;
        priceAfterTax = priceAfterTax.add(priceAfterTax.multiply(getTax()));
        return priceAfterTax.setScale(2, RoundingMode.HALF_UP);
    }

    public static <T> BigDecimal getPriceAfterTax(List<T> userItems,
                                              Function<T, Integer> quantityGetter,
                                              Function<T, BigDecimal> salePriceGetter,
                                              Function<T, BigDecimal> priceGetter) {
        BigDecimal priceBeforeTax = getPriceBeforeTax(userItems, quantityGetter, salePriceGetter, priceGetter);
        return getPriceAfterTax(priceBeforeTax);
    }

    public static BigDecimal getTax() {
        return new BigDecimal("0.0625");
    }

    @Transactional(readOnly = true)
    public ShortProductDTO getShortProductInfo(Product product, boolean getFeatures) {
        ShortProductDTO shortProductDTO = (getFeatures) ? productMapper.toShortProductWithFeaturesDTO(product)
                : productMapper.toShortProductWithoutFeaturesDTO(product);
        shortProductDTO.setImageName(
                product.getThumbnail() != null ? product.getThumbnail() :
                product.getMedia().stream().findFirst().map(ProductMedia::getContent).orElse(null)
        );
        shortProductDTO.setDiscountedPrice(
                product.getSaleEndDate() == null ? null : product.getSaleEndDate().isAfter(LocalDate.now()) ? product.getSalePrice() : null
        );
        if (product.getSaleEndDate() != null) {
            long daysDifference = ChronoUnit.DAYS.between(product.getSaleEndDate(), LocalDate.now());
            shortProductDTO.setNewRelease(daysDifference >= 0 && daysDifference < 8);
        } else {
            shortProductDTO.setNewRelease(false);
        }
        return shortProductDTO;
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
        TransactionSynchronizationManager.registerSynchronization(mediaService.getMediaTransactionSynForSaving(filenameList));

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

            mediaList.add(new ProductMedia(savedProduct, mediaDTO.contentType(), mediaName, j));
        }
        productMediaRepository.saveAll(mediaList);
        savedProduct.setThumbnail(mediaList.isEmpty() ? null : mediaList.getFirst().getContent());

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

        logger.info("Saved product: {}", savedProduct.getId());

        return savedProduct.getId();
    }

    @Transactional
    public Long updateProductInfo(ProductDTO productDTO, Integer productLineId, Map<String, MultipartFile> fileMap) {
        Product product = findProductById(productDTO.getId());

        List<String> updatedFilenames = new ArrayList<>();

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
        if (productDTO.getSalePrice() == null || !productDTO.getSalePrice().equals(product.getSalePrice()))
            product.setSalePrice(productDTO.getSalePrice());
        if (productDTO.getSaleEndDate() == null || !productDTO.getSaleEndDate().equals(product.getSaleEndDate()))
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
        List<ProductMedia> oldMedia = product.getMedia();
        List<String> oldFilenames = new ArrayList<>(oldMedia.stream()
                .map(ProductMedia::getContent)
                .toList());
        Map<Long, ProductMedia> currentMediaMap = oldMedia
                .stream()
                .collect(Collectors.toMap(ProductMedia::getId, m -> m)); // to get existing media entity by id quickly
        List<ProductMedia> updatedMediaList = mediaService.buildUpdatedMediaList(
                productDTO.getMedia(),
                currentMediaMap,
                (dto, sortOrder) -> new ProductMedia(product, dto.contentType(),
                        mediaService.saveMedia(fileMap.get(dto.content()), true), sortOrder)
        );
        updatedFilenames.addAll(updatedMediaList.stream()
                .map(ProductMedia::getContent)
                .toList());

        product.getMedia().clear();
        product.getMedia().addAll(updatedMediaList);

        if (!updatedMediaList.isEmpty()) {
            if (!product.getThumbnail().equals(updatedMediaList.getFirst().getContent()))
                product.setThumbnail(updatedMediaList.getFirst().getContent());
        }

        // update description
        List<ProductDescription> oldDescription = product.getDescriptions();
        oldFilenames.addAll(oldDescription.stream()
                .filter(d -> d.getContentType().isMedia())
                .map(ProductDescription::getContent)
                .toList());
        Map<Long, ProductDescription> currentDescriptionMap = oldDescription.stream() // to get existing media entity by id quickly
                .collect(Collectors.toMap(ProductDescription::getId, d -> d));
        List<ProductDescription> updatedDescriptionList = mediaService.buildUpdatedMediaList(
                productDTO.getDescriptions(),
                currentDescriptionMap,
                (dto, sortOrder) -> new ProductDescription(product, dto.contentType(),
                        dto.contentType().isMedia() ? mediaService.saveMedia(fileMap.get(dto.content()), true) : dto.content(), sortOrder)
        );
        updatedFilenames.addAll(updatedDescriptionList.stream()
                .filter(d -> d.getContentType().isMedia())
                .map(ProductDescription::getContent)
                .toList());

        product.getDescriptions().clear();
        product.getDescriptions().addAll(updatedDescriptionList);

        // update option
        ProductLine productLine = productLineId == null ? product.getProductLine() : productLineService.findProductLineById(productLineId);
        product.setProductLine(productLine);

        Map<String, ProductOption> currentOptionMap = product.getOptions()
                .stream()
                .collect(Collectors.toMap(ProductOption::getName, o -> o));
        List<ProductOption> updatedOptionList = buildUpdateOptionList(
                productDTO.getOptions(),
                currentOptionMap,
                (dto) -> new ProductOption(product, productLine, dto.name(), dto.valueOption())
        );
        product.getOptions().clear();
        product.getOptions().addAll(updatedOptionList);

        Map<String, ProductSpecification> currentSpecificationMap = product.getSpecifications()
                .stream()
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

        List<String> deletedFilenames = oldFilenames.stream().filter(f -> !updatedFilenames.contains(f)).toList();
        List<String> addedFilenames = updatedFilenames.stream().filter(f -> !oldFilenames.contains(f)).toList();

        TransactionSynchronizationManager.registerSynchronization(mediaService.getMediaTransactionSynForUpdating(
                deletedFilenames, addedFilenames
        ));

        try {
            mediaService.movePermanentToTemp(deletedFilenames);
        } catch (IOException e) {
            throw new RuntimeException("Fail to update media");
        }

        Product updatedProduct = productRepository.save(product);

        logger.info("Updated product: {}", updatedProduct.getId());

        return updatedProduct.getId();
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

    @Transactional
    public void deleteProductById(Long id) {
        Product product = findProductById(id);
        ProductLine productLine = product.getProductLine();

        List<String> filenameList = new ArrayList<>();

        TransactionSynchronizationManager.registerSynchronization(mediaService.getMediaTransactionSynForDeleting(filenameList));

        filenameList.addAll(product.getMedia()
                .stream()
                .map(ProductMedia::getContent)
                .toList());
        filenameList.addAll(product.getDescriptions()
                .stream()
                .filter(d -> d.getContentType().isMedia())
                .map(ProductDescription::getContent)
                .toList());

        productRepository.delete(product);

        try {
            mediaService.movePermanentToTemp(filenameList);
        } catch (IOException e) {
            throw new RuntimeException("Fail to delete product line media");
        }

        if (productLine.getProducts().isEmpty()) {
            productLineService.deleteProductLineById(productLine.getId());
        }

        logger.info("Deleted product: {}", id);
    }
}

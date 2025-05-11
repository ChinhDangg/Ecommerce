package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.*;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductLineRepository productLineRepository;
    private final ProductLineMediaRepository productLineMediaRepository;
    private final ProductLineDescriptionRepository productLineDescriptionRepository;
    private final ProductRepository productRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final ProductMediaRepository productMediaRepository;
    private final ProductDescriptionRepository productDescriptionRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductSpecificationRepository productSpecificationRepository;

    public ProductService(
            ProductCategoryRepository productCategoryRepository,
            ProductLineRepository productLineRepository,
            ProductLineMediaRepository productLineMediaRepository,
            ProductLineDescriptionRepository productLineDescriptionRepository,
            ProductRepository productRepository,
            ProductFeatureRepository productFeatureRepository,
            ProductMediaRepository productMediaRepository,
            ProductDescriptionRepository productDescriptionRepository,
            ProductOptionRepository productOptionRepository,
            ProductSpecificationRepository productSpecificationRepository
    ) {
        this.productCategoryRepository = productCategoryRepository;
        this.productLineRepository = productLineRepository;
        this.productLineMediaRepository = productLineMediaRepository;
        this.productLineDescriptionRepository = productLineDescriptionRepository;
        this.productRepository = productRepository;
        this.productFeatureRepository = productFeatureRepository;
        this.productMediaRepository = productMediaRepository;
        this.productDescriptionRepository = productDescriptionRepository;
        this.productOptionRepository = productOptionRepository;
        this.productSpecificationRepository = productSpecificationRepository;
    }

    public List<ProductCategoryDTO> findAllTopCategory() {
        return productCategoryRepository.findAllTopParentCategory();
    }

    public List<ProductCategoryDTO> findAllSubCategoryOf(int id) {
        ProductCategory category = productCategoryRepository.findById(id).orElse(null);
        if (category == null)
            return null;
        return category.getSubcategories()
                .stream()
                .map(c -> new ProductCategoryDTO(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public ProductCategoryDTO saveProductCategory(ProductCategoryDTO productCategoryDTO) {
        Integer id = productCategoryDTO.getId();
        ProductCategory parentCategory = (id == null) ? null : productCategoryRepository
                .findById(id).orElse(null);
        ProductCategory newCategory = new ProductCategory(
                productCategoryDTO.getName(),
                parentCategory
        );

        ProductCategory createdCategory = productCategoryRepository.save(newCategory);
        return new ProductCategoryDTO(createdCategory.getId(), createdCategory.getName());
    }

    @Transactional
    public Integer saveProductLine(ProductLineDTO productLineDTO) {
        if (productLineDTO.getName() == null)
            throw new IllegalStateException("Product line name is null");
        ProductLine savedProductLine = productLineRepository.save(new ProductLine(productLineDTO.getName()));

        List<ProductLineMedia> mediaList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getImageNames().length; j++) {
            mediaList.add(new ProductLineMedia(
                    savedProductLine,
                    productLineDTO.getImageNames()[j],
                    j
            ));
        }
        productLineMediaRepository.saveAll(mediaList);

        List<ProductLineDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getDescriptions().length; j++) {
            descriptionList.add(new ProductLineDescription(
                    savedProductLine,
                    productLineDTO.getDescriptions()[j].type(),
                    productLineDTO.getDescriptions()[j].content(),
                    j
            ));
        }
        productLineDescriptionRepository.saveAll(descriptionList);
        return savedProductLine.getId();
    }

    @Transactional
    public Long saveProduct(ProductDTO productDTO) {
        if (productDTO.getName() == null)
            throw new IllegalStateException("Product name is null");
        ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId()).orElse(null);
        ProductLine productLine = productDTO.getProductLineId() == null
                ? null : productLineRepository.findById(productDTO.getProductLineId()).orElse(null);
        Product savedProduct = productRepository.save(new Product(
                productDTO.getManufacturerId(),
                productDTO.getName(),
                productDTO.getBrand(),
                productDTO.getQuantity(),
                productDTO.getConditionType(),
                LocalDate.now(),
                new BigDecimal(productDTO.getRegularPrice()),
                new BigDecimal(productDTO.getSalePrice()),
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
        for (int j = 0; j < productDTO.getImageNames().length; j++) {
            mediaList.add(new ProductMedia(
                    savedProduct,
                    productDTO.getImageNames()[j],
                    j
            ));
        }
        if (!mediaList.isEmpty())
            productMediaRepository.saveAll(mediaList);

        List<ProductDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productDTO.getDescriptions().length; j++) {
            descriptionList.add(new ProductDescription(
                    savedProduct,
                    productDTO.getDescriptions()[j].type(),
                    productDTO.getDescriptions()[j].content(),
                    j
            ));
        }
        if (!descriptionList.isEmpty())
            productDescriptionRepository.saveAll(descriptionList);

        List<ProductOption> optionList = new ArrayList<>();
        for (Option option : productDTO.getOptions()) {
            optionList.add(new ProductOption(savedProduct, option.name(), option.value()));
        }
        if (!optionList.isEmpty())
            productOptionRepository.saveAll(optionList);

        List<ProductSpecification> specificationList = new ArrayList<>();
        for (Specification specification : productDTO.getSpecifications()) {
            specificationList.add(new ProductSpecification(savedProduct, specification.name(), specification.value()));
        }
        if (!specificationList.isEmpty())
            productSpecificationRepository.saveAll(specificationList);

        return savedProduct.getId();
    }
}

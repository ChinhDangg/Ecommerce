package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.*;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final ProductMapper productMapper;

    private final EntityManager entityManager;

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
            ProductSpecificationRepository productSpecificationRepository,
            ProductMapper productMapper,
            EntityManager entityManager
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
        this.productMapper = productMapper;
        this.entityManager = entityManager;
    }

    public List<ProductCategoryDTO> findAllTopCategory() {
        return productCategoryRepository.findAllTopParentCategory();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> findAllSubCategoryOf(int id) {
        ProductCategory category = productCategoryRepository.findById(id).orElse(null);
        if (category == null)
            return null;
        return productMapper.toProductCategoryDTOList(category.getSubcategories());
    }

    @Transactional(readOnly = true)
    public ProductLineDTO findProductLineById(Integer id) {
        ProductLine productLine = productLineRepository.findById(id).orElse(null);
        if (productLine == null)
            return null;
        productLine.getDescriptions().size();
        productLine.getMedia().size();

        return productMapper.toProductLineDTO(productLine);
    }

    @Transactional(readOnly = true)
    public Page<ShortProductDTO> findProductsByName(String productName, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Specification<Product> spec = ProductSpecifications.nameContainsWords(productName);

        if (spec == null)
            return new PageImpl<>(new ArrayList<>(), pageable, 0);;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        Predicate predicate = spec.toPredicate(root, query, cb);
        query.where(predicate);

        query.select(cb.construct(
                Product.class,
                root.get("id"),
                root.get("manufacturerId"),
                root.get("name"),
                root.get("quantity"),
                root.get("price"),
                root.get("salePrice"),
                root.get("saleEndDate")
        ));

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Product> resultList = typedQuery.getResultList();
        List<ShortProductDTO> shortProductDTOList = new ArrayList<>();
        for (Product product : resultList) {
            product.getMedia().size();
            ShortProductDTO current = productMapper.toShortProductWithoutFeaturesDTO(product);
            current.setImageName(product.getMedia().isEmpty() ? null : product.getMedia().getFirst().getContent());
            current.setDiscountedPrice(
                    product.getSaleEndDate() == null ? null : product.getSaleEndDate().isAfter(LocalDate.now()) ? product.getSalePrice() : null
            );
            shortProductDTOList.add(current);
        }

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot)).where(spec.toPredicate(countRoot, countQuery, cb));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(shortProductDTOList, pageable, total);
    }

    @Transactional(readOnly = true)
    public ProductDTO findProductById(Long id) {
        Product foundProduct = productRepository.findById(id).orElse(null);
        if (foundProduct == null)
            return null;
        foundProduct.getProductLine();
        foundProduct.getOptions().size();
        foundProduct.getSpecifications().size();
        foundProduct.getFeatures().size();
        foundProduct.getMedia().size();
        foundProduct.getDescriptions().size();
        return productMapper.toProductDTO(foundProduct);
    }

    public void findProductGroupedOptions(Integer productLineId) {
        if (productLineId == null)
            throw new IllegalStateException("Passing null Product line id");
        List<ProductOptionGroupProjection> groupedOptions = productOptionRepository.findProductOptionByProductLine(productLineId);
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
        for (int j = 0; j < productLineDTO.getMedia().size(); j++) {
            mediaList.add(new ProductLineMedia(
                    savedProductLine,
                    productLineDTO.getMedia().get(j).contentType(),
                    productLineDTO.getMedia().get(j).content(),
                    j
            ));
        }
        productLineMediaRepository.saveAll(mediaList);

        List<ProductLineDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getDescriptions().size(); j++) {
            descriptionList.add(new ProductLineDescription(
                    savedProductLine,
                    productLineDTO.getDescriptions().get(j).contentType(),
                    productLineDTO.getDescriptions().get(j).content(),
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
                new BigDecimal(productDTO.getPrice()),
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
            optionList.add(new ProductOption(savedProduct, productLine, option.name(), option.value()));
        }
        if (!optionList.isEmpty())
            productOptionRepository.saveAll(optionList);

        List<ProductSpecification> specificationList = new ArrayList<>();
        for (OptionDTO specification : productDTO.getSpecifications()) {
            specificationList.add(new ProductSpecification(savedProduct, specification.name(), specification.value()));
        }
        if (!specificationList.isEmpty())
            productSpecificationRepository.saveAll(specificationList);

        return savedProduct.getId();
    }
}

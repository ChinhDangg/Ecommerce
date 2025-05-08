package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.entity.ProductCategory;
import dev.ecommerce.product.entity.ProductLine;
import dev.ecommerce.product.entity.ProductLineDescription;
import dev.ecommerce.product.entity.ProductLineMedia;
import dev.ecommerce.product.repository.ProductCategoryRepository;
import dev.ecommerce.product.repository.ProductLineDescriptionRepository;
import dev.ecommerce.product.repository.ProductLineMediaRepository;
import dev.ecommerce.product.repository.ProductLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductLineRepository productLineRepository;
    private final ProductLineMediaRepository productLineMediaRepository;
    private final ProductLineDescriptionRepository productLineDescriptionRepository;

    public ProductService(
            ProductCategoryRepository productCategoryRepository,
            ProductLineRepository productLineRepository,
            ProductLineMediaRepository productLineMediaRepository,
            ProductLineDescriptionRepository productLineDescriptionRepository
    ) {
        this.productCategoryRepository = productCategoryRepository;
        this.productLineRepository = productLineRepository;
        this.productLineMediaRepository = productLineMediaRepository;
        this.productLineDescriptionRepository = productLineDescriptionRepository;
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
        if (productLineDTO.getProductLineName() == null)
            throw new IllegalStateException("Product line name is null");
        ProductLine savedProductLine = productLineRepository.save(new ProductLine(productLineDTO.getProductLineName()));

        List<ProductLineMedia> mediaList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getProductLineImageNames().length; j++) {
            mediaList.add(new ProductLineMedia(
                    savedProductLine,
                    productLineDTO.getProductLineImageNames()[j],
                    j
            ));
        }
        productLineMediaRepository.saveAll(mediaList);

        List<ProductLineDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getProductLineDescriptions().length; j++) {
            descriptionList.add(new ProductLineDescription(
                    savedProductLine,
                    productLineDTO.getProductLineDescriptions()[j].type(),
                    productLineDTO.getProductLineDescriptions()[j].content(),
                    j
            ));
        }
        productLineDescriptionRepository.saveAll(descriptionList);
        return savedProductLine.getId();
    }
}

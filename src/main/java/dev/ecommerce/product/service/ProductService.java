package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.entity.ProductCategory;
import dev.ecommerce.product.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
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
        ProductCategory newCategory = new ProductCategory();
        newCategory.setName(productCategoryDTO.getName());
        newCategory.setParentProductCategory(parentCategory);

        ProductCategory createdCategory = productCategoryRepository.save(newCategory);
        return new ProductCategoryDTO(createdCategory.getId(), createdCategory.getName());
    }
}

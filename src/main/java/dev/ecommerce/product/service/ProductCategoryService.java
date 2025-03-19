package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.entity.ProductCategory;
import dev.ecommerce.product.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
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
}

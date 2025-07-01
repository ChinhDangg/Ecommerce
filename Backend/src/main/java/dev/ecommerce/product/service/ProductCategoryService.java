package dev.ecommerce.product.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.entity.ProductCategory;
import dev.ecommerce.product.repository.ProductCategoryRepository;
import dev.ecommerce.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository,
                                  ProductRepository productRepository,
                                  ProductService productService,
                                  ProductMapper productMapper) {
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.productMapper = productMapper;
    }

    private ProductCategory findProductCategoryById(Integer id, boolean nullable) {
        Optional<ProductCategory> productCategory = productCategoryRepository.findById(id);
        return (nullable)
                ? productCategory.orElse(null)
                : productCategory.orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public List<ProductCategoryDTO> findAllTopCategory() {
        return productCategoryRepository.findAllTopParentCategory();
    }

    public List<ProductCategoryDTO> findCategorySameParentCategoriesById(Long id) {
        ProductCategory productCategory = productService.getProductById(id).getCategory();

        ProductCategory parentCategory = productCategory.getParentProductCategory();
        return (parentCategory == null)
                ? productMapper.toProductCategoryDTOList(List.of(productCategory))
                : productMapper.toProductCategoryDTOList(parentCategory.getSubcategories());
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> findAllSubCategoryOf(Integer id) {
        ProductCategory category = findProductCategoryById(id, false);
        return productMapper.toProductCategoryDTOList(category.getSubcategories());
    }

    @Transactional
    public ProductCategoryDTO saveProductCategory(ProductCategoryDTO productCategoryDTO) {
        Integer id = productCategoryDTO.getId();
        ProductCategory parentCategory = (id == null) ? null : findProductCategoryById(id, true);
        ProductCategory newCategory = new ProductCategory(
                productCategoryDTO.getName(),
                parentCategory
        );

        ProductCategory createdCategory = productCategoryRepository.save(newCategory);
        return new ProductCategoryDTO(createdCategory.getId(), createdCategory.getName());
    }

    @Transactional
    public List<Long> updateProductCategory(List<Long> productIds, Integer categoryId) {
        ProductCategory category = findProductCategoryById(categoryId, false);
        List<Long> updatedProductIds = new ArrayList<>();
        for (Long productId : productIds) {
            Product product = productService.getProductById(productId);
            product.setCategory(category);
            updatedProductIds.add(productRepository.save(product).getId());
        }
        return updatedProductIds;
    }
}

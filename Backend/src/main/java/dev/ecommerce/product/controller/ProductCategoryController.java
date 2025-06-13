package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.DTO.ProductCategoryUpdateDTO;
import dev.ecommerce.product.service.ProductCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping("/top")
    public List<ProductCategoryDTO> getTopCategories() {
        return productCategoryService.findAllTopCategory();
    }

    @GetMapping("/parent/{productId}")
    public List<ProductCategoryDTO> getSameParentCategories(@PathVariable Long productId) {
        return productCategoryService.findCategorySameParentCategoriesById(productId);
    }

    @GetMapping("/subcategory/{id}")
    public List<ProductCategoryDTO> getSubCategories(@PathVariable Integer id) {
        return productCategoryService.findAllSubCategoryOf(id);
    }

    @PostMapping()
    public ResponseEntity<ProductCategoryDTO> addCategory(@RequestBody ProductCategoryDTO categoryDTO) {
        ProductCategoryDTO cat = productCategoryService.saveProductCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cat);
    }

    @PutMapping()
    public ResponseEntity<List<Long>> updateCategory(@RequestBody ProductCategoryUpdateDTO productCategoryUpdateDTO) {
        List<Long> updatedProductIds = productCategoryService.updateProductCategory(
                productCategoryUpdateDTO.productIds(),
                productCategoryUpdateDTO.categoryId()
        );
        return ResponseEntity.status(HttpStatus.OK).body(updatedProductIds);
    }
}

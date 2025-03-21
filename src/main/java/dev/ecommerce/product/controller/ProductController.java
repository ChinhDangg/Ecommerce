package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService categoryService;
    private final ProductService productService;

    public ProductController(ProductService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/category")
    public List<ProductCategoryDTO> topCategories() {
        return categoryService.findAllTopCategory();
    }

    @GetMapping("/category/{id}")
    public List<ProductCategoryDTO> subCategories(@PathVariable int id) {
        return categoryService.findAllSubCategoryOf(id);
    }

    @PostMapping("/category/new")
    public ResponseEntity<ProductCategoryDTO> addCategory(@RequestBody ProductCategoryDTO categoryDTO) {
        ProductCategoryDTO cat = productService.saveProductCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cat);
    }

}

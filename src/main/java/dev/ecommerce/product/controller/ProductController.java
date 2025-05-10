package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

    @GetMapping("/subcategory/{id}")
    public List<ProductCategoryDTO> subCategories(@PathVariable int id) {
        return categoryService.findAllSubCategoryOf(id);
    }

    @PostMapping("/category/new")
    public ResponseEntity<ProductCategoryDTO> addCategory(@RequestBody ProductCategoryDTO categoryDTO) {
        ProductCategoryDTO cat = productService.saveProductCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cat);
    }

    @PostMapping("/newProductLine")
    public ResponseEntity<Integer> addProduct(@Valid @RequestBody ProductLineDTO productLineDTO) {
        Integer savedProductLineId = categoryService.saveProductLine(productLineDTO);
        if (savedProductLineId == null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        return ResponseEntity.status(HttpStatus.OK).body(savedProductLineId);
    }

    @PostMapping("/uploadImages")
    public ResponseEntity<List<String>> handleFileUpload(@RequestParam("images") MultipartFile[] images) {
        try {
            System.out.println("Received " + images.length + " images");
            // Process each file
            List<String> fileNames = Arrays.stream(images)
                    .map(file -> {
                        try {
                            // Save file to a directory (e.g., uploads/)
                            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                            Path path = Paths.get("uploads/" + fileName);
                            Files.createDirectories(path.getParent()); // Ensure directory exists
                            Files.write(path, file.getBytes());
                            return fileName;
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
                        }
                    })
                    .toList();
            System.out.println(fileNames);
            return ResponseEntity.ok(fileNames);
        } catch (Exception e) {
            System.out.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}

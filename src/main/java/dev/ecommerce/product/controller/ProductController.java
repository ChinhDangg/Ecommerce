package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.DTO.ProductDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/category")
    public List<ProductCategoryDTO> getTopCategories() {
        return productService.findAllTopCategory();
    }

    @GetMapping("/subcategory/{id}")
    public List<ProductCategoryDTO> getSubCategories(@PathVariable int id) {
        return productService.findAllSubCategoryOf(id);
    }

    @GetMapping("/productLine/{productLineId}")
    public ProductLineDTO getProductLine(@PathVariable int productLineId) {
        return productService.findProductLineById(productLineId);
    }

    @GetMapping("/search/product")
    public Page<ProductDTO> getProducts(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "") String search) {
        return productService.findProductsByName(search, page);
    }

    @PostMapping("/category/new")
    public ResponseEntity<ProductCategoryDTO> addCategory(@RequestBody ProductCategoryDTO categoryDTO) {
        ProductCategoryDTO cat = productService.saveProductCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cat);
    }

    @PostMapping("/newProductLine")
    public ResponseEntity<Integer> addProductLine(@Valid @RequestBody ProductLineDTO productLineDTO) {
        Integer savedProductLineId = productService.saveProductLine(productLineDTO);
        if (savedProductLineId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProductLineId);
    }

    @PostMapping("/newProduct")
    public ResponseEntity<Long> addProduct(@Valid @RequestBody ProductDTO productDTO) {
        Long savedProductId = productService.saveProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProductId);
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
                            Path path = Paths.get("/static/images" + fileName);
                            Files.createDirectories(path.getParent()); // Ensure directory exists
                            Files.write(path, file.getBytes());
                            return fileName;
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
                        }
                    })
                    .toList();
            System.out.println(fileNames);
            return ResponseEntity.status(HttpStatus.CREATED).body(fileNames);
        } catch (Exception e) {
            System.out.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}

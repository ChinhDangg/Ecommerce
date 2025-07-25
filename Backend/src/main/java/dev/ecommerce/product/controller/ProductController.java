package dev.ecommerce.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.ecommerce.product.DTO.ProductDTO;
import dev.ecommerce.product.DTO.ProductSearchResultDTO;
import dev.ecommerce.product.DTO.ShortProductDTO;
import dev.ecommerce.product.constant.SortOption;
import dev.ecommerce.product.service.ProductSearchService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService, ProductSearchService productSearchService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    @GetMapping("/by-name")
    public ResponseEntity<ProductSearchResultDTO> searchProducts(@RequestParam Map<String, String> allParams) {
        String searchString = allParams.remove("q");
        String pageStr = allParams.remove("page");
        int page = Integer.parseInt(pageStr != null ? pageStr : "0");
        String featureStr = allParams.remove("feature");
        boolean getFeatures = Boolean.parseBoolean(featureStr != null ? featureStr : "false");
        String sortStr = allParams.remove("sort");
        SortOption sortBy = null;
        if (sortStr != null) {
            try {
                sortBy = SortOption.valueOf(sortStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                sortBy = SortOption.RELEVANCE;
            }
        }

        String filterParam = allParams.remove("filters"); // e.g., GPU:4090|4080,RAM:32GB|64GB
        Map<String, List<String>> selectedFilters = parseFilterParam(filterParam);

        return ResponseEntity.status(HttpStatus.OK).body(productSearchService.searchProductByName(searchString, page, getFeatures, sortBy, selectedFilters));
    }
    private Map<String, List<String>> parseFilterParam(String filterParam) {
        Map<String, List<String>> selectedFilters = new HashMap<>();

        if (filterParam != null && !filterParam.isEmpty()) {
            String[] filterPairs = filterParam.split(",");
            for (String pair : filterPairs) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    String name = parts[0];
                    List<String> values = Arrays.asList(parts[1].split("\\|"));
                    selectedFilters.put(name, values);
                }
            }
        }
        return selectedFilters;
    }

    @GetMapping("/by-category")
    public ResponseEntity<Page<ShortProductDTO>> searchProductsByCategory(
            @RequestParam(defaultValue = "1") Integer id,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findProductsByCategory(id, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO productDTO = productService.findProductById(id);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Long> addProduct(@Valid @RequestBody ProductDTO productDTO) {
        Long savedProductId = productService.saveProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProductId);
    }

    @PutMapping()
    public ResponseEntity<Long> updateProduct(@Valid @RequestBody ProductDTO productDTO) {
        Long updatedProductId = productService.updateProductInfo(productDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProductId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
                            Path path = Paths.get("C:\\Users\\minhc\\IdeaProjects\\Ecommerce\\FrontEnd\\src\\main\\resources\\static\\images\\" + fileName);
                            Files.createDirectories(path.getParent()); // Ensure directory exists
                            Files.write(path, file.getBytes());
                            return "/images/" + path.getFileName().toString();
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

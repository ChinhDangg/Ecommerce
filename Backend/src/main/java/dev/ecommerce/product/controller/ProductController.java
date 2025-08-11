package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductDTO;
import dev.ecommerce.product.DTO.ProductSearchResultDTO;
import dev.ecommerce.product.constant.SortOption;
import dev.ecommerce.product.service.ProductSearchService;
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

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResultDTO> searchProducts(@RequestParam Map<String, String> allParams) {
        String searchString = allParams.remove("q");

        String pageStr = allParams.remove("page");
        int page = 0;
        if (pageStr != null) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (IllegalArgumentException _) {}
        }

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

        String specialFilterParam = allParams.remove("s-filters");
        Map<String, List<String>> specialFilters = parseFilterParam(specialFilterParam);

        String filterParam = allParams.remove("filters"); // e.g., GPU:4090|4080,RAM:32GB|64GB
        Map<String, List<String>> selectedFilters = parseFilterParam(filterParam);

        String categoryIdStr = allParams.remove("cateId");
        if (categoryIdStr != null) {
            try {
                int categoryId = Integer.parseInt(categoryIdStr);
                return ResponseEntity.status(HttpStatus.OK).body(productSearchService.findProductsByCategory(
                        categoryId, page, 10, getFeatures, sortBy, specialFilters, selectedFilters));
            } catch (IllegalArgumentException _) {
                return ResponseEntity.badRequest().body(null);
            }
        } else if (searchString == null) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                productSearchService.searchProductByName(searchString, page, 10, getFeatures, sortBy, specialFilters, selectedFilters));
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

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO productDTO = productService.getProductDTOById(id);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Long> addProduct(@Valid @RequestPart ProductDTO productDTO,
                                           @RequestParam(required = false) Map<String, MultipartFile> fileMap) {
        Long savedProductId = productService.saveProduct(productDTO, null, fileMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProductId);
    }

    @PutMapping()
    public ResponseEntity<Long> updateProduct(@Valid @RequestPart ProductDTO productDTO,
                                              @RequestParam(required = false) Map<String, MultipartFile> fileMap) {
        Long updatedProductId = productService.updateProductInfo(productDTO, fileMap);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProductId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

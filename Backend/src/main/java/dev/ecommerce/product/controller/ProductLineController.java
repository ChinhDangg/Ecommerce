package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.service.ProductLineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productLine")
public class ProductLineController {

    private final ProductLineService productLineService;

    public ProductLineController(ProductLineService productLineService) {
        this.productLineService = productLineService;
    }

    @GetMapping("/{productLineId}")
    public ProductLineDTO getProductLine(@PathVariable int productLineId) {
        return productLineService.findProductLineById(productLineId);
    }

    @PostMapping("/new")
    public ResponseEntity<Integer> addProductLine(@Valid @RequestBody ProductLineDTO productLineDTO) {
        Integer savedProductLineId = productLineService.saveProductLine(productLineDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProductLineId);
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<Integer> updateProductLine(@PathVariable int id, @Valid @RequestBody ProductLineDTO productLineDTO) {
        Integer updatedProductLineId = productLineService.updateProductLine(id, productLineDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProductLineId);
    }


}

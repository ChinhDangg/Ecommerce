package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.service.ProductLineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/productLine")
public class ProductLineController {

    private final ProductLineService productLineService;

    public ProductLineController(ProductLineService productLineService) {
        this.productLineService = productLineService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductLineDTO> getProductLine(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(productLineService.getProductLineDTOById(id));
    }

    @PostMapping()
    public ResponseEntity<Integer> addProductLine(@Valid @RequestPart ProductLineDTO productLineDTO,
                                                  @RequestPart(required = false) Map<String, MultipartFile> fileMap) {
        Integer savedProductLineId = productLineService.saveProductLine(productLineDTO, fileMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProductLineId);
    }

    @PutMapping()
    public ResponseEntity<Integer> updateProductLine(@Valid @RequestBody ProductLineDTO productLineDTO) {
        Integer updatedProductLineId = productLineService.updateProductLineInfo(productLineDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProductLineId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProductLine(@PathVariable Integer id) {
        productLineService.deleteProductLineById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

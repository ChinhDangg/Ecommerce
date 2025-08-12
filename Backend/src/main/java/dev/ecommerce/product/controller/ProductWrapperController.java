package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.*;
import dev.ecommerce.product.service.ProductWrapperService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productWrapper")
public class ProductWrapperController {

    private final ProductWrapperService productWrapperService;

    ProductWrapperController(ProductWrapperService productWrapperService) {
        this.productWrapperService = productWrapperService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductLineDTO> getProductLine(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productWrapperService.getProductLineDTObyProductId(id));
    }

    @PostMapping
    public ResponseEntity<List<Long>> addAllProductInfo(@Valid @RequestPart ProductWrapperDTO productWrapperDTO,
                                                        @RequestParam(required = false) Map<String, MultipartFile> fileMap) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                productWrapperService.saveAllProductInfo(
                        productWrapperDTO.productLineDTO(),
                        productWrapperDTO.productDTOList(),
                        fileMap
                )
        );
    }

    @PutMapping
    public ResponseEntity<List<Long>> updateAllProductInfo(@RequestPart ProductUpdateDTO productUpdateDTO,
                                                           @RequestParam(required = false) Map<String, MultipartFile> fileMap) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productWrapperService.updateAllProductInfo(
                        productUpdateDTO.productLineDTO(),
                        productUpdateDTO.updatingProductDTOList(),
                        productUpdateDTO.newProductDTOList(),
                        fileMap
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllProductInfo(@Valid @RequestBody ProductDeleteDTO productDeleteDTO) {
        productWrapperService.deleteAllProductInfo(productDeleteDTO.productLineId(), productDeleteDTO.productIdList());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/card/{id}")
    public ResponseEntity<ProductCardDTO> getProductCardDTO(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productWrapperService.getProductCardById(id)
        );
    }

}

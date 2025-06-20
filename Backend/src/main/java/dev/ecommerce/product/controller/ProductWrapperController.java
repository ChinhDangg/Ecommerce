package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductDeleteDTO;
import dev.ecommerce.product.DTO.ProductUpdateWrapperDTO;
import dev.ecommerce.product.service.ProductWrapperService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productWrapper")
public class ProductWrapperController {

    private final ProductWrapperService productWrapperService;

    ProductWrapperController(ProductWrapperService productWrapperService) {
        this.productWrapperService = productWrapperService;
    }

    @PutMapping
    public ResponseEntity<List<Long>> updateAllProductInfo(@Valid @RequestBody ProductUpdateWrapperDTO productUpdateWrapperDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productWrapperService.updateAllProductInfo(
                        productUpdateWrapperDTO.productLineDTO(),
                        productUpdateWrapperDTO.productDTOList()
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllProductInfo(@Valid @RequestBody ProductDeleteDTO productDeleteDTO) {
        productWrapperService.deleteAllProductInfo(productDeleteDTO.productLineId(), productDeleteDTO.productIdList());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

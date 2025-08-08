package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductCardDTO;
import dev.ecommerce.product.DTO.ProductDeleteDTO;
import dev.ecommerce.product.DTO.ProductUpdateDTO;
import dev.ecommerce.product.DTO.ProductWrapperDTO;
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

    @PostMapping
    public ResponseEntity<List<Long>> addAllProductInfo(@Valid @RequestBody ProductWrapperDTO productWrapperDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                productWrapperService.saveAllProductInfo(
                        productWrapperDTO.productLineDTO(),
                        productWrapperDTO.productDTOList()
                )
        );
    }

    @PutMapping
    public ResponseEntity<List<Long>> updateAllProductInfo(@RequestBody ProductUpdateDTO productUpdateDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productWrapperService.updateAllProductInfo(
                        productUpdateDTO.productLineDTO(),
                        productUpdateDTO.updatingProductDTOList(),
                        productUpdateDTO.newProductDTOList()
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

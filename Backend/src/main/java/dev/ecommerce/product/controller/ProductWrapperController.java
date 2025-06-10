package dev.ecommerce.product.controller;

import dev.ecommerce.product.DTO.ProductWrapperDTO;
import dev.ecommerce.product.service.ProductWrapperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productWrapper")
public class ProductWrapperController {

    private final ProductWrapperService productWrapperService;

    ProductWrapperController(ProductWrapperService productWrapperService) {
        this.productWrapperService = productWrapperService;
    }

    @PutMapping
    public ResponseEntity<List<Long>> updateAllProductInfo(@RequestBody ProductWrapperDTO productWrapperDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productWrapperService.updateAllProductInfo(
                        productWrapperDTO.productLineDTO(),
                        productWrapperDTO.productDTOList()
                )
        );
    }

}

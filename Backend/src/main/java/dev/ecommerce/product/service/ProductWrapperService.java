package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.ProductDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductWrapperService {

    private final ProductLineService productLineService;
    private final ProductService productService;

    public ProductWrapperService(ProductLineService productLineService, ProductService productService) {
        this.productLineService = productLineService;
        this.productService = productService;
    }

    @Transactional
    public List<Long> updateAllProductInfo(ProductLineDTO productLineDTO, List<ProductDTO> productDTOList) {
        List<Long> updatedIds = new ArrayList<>();
        updatedIds.add(productLineService.updateProductLineInfo(productLineDTO).longValue());
        for (ProductDTO productDTO : productDTOList) {
            updatedIds.add(productService.updateProductInfo(productDTO));
        }
        return updatedIds;
    }

    @Transactional
    public void deleteAllProductInfo(Integer productLineId, List<Long> productIdList) {
        if (productLineId != null)
            productLineService.deleteProductLineById(productLineId);
        for (Long productId : productIdList)
            productService.deleteProductById(productId);
    }
}

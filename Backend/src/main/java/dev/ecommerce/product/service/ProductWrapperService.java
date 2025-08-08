package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.ProductCardDTO;
import dev.ecommerce.product.DTO.ProductDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductWrapperService {

    private final ProductLineService productLineService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final ProductMapper productMapper;

    public ProductWrapperService(ProductLineService productLineService,
                                 ProductService productService,
                                 ProductCategoryService productCategoryService,
                                 ProductMapper productMapper) {
        this.productLineService = productLineService;
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.productMapper = productMapper;
    }

    @Transactional
    public List<Long> saveAllProductInfo(ProductLineDTO productLineDTO, List<ProductDTO> productDTOList) {
        List<Long> savedIds = new ArrayList<>();
        Integer productLineId = null;
        if (productLineDTO != null) {
            productLineId = productLineService.saveProductLine(productLineDTO);
            savedIds.add(productLineId.longValue());
        }
        for (ProductDTO productDTO : productDTOList) {
            savedIds.add(productService.saveProduct(productDTO, productLineId));
        }
        return savedIds;
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

    @Transactional(readOnly = true)
    public ProductCardDTO getProductCardById(Long id) {
        Product product = productService.findProductById(id);
        ProductCardDTO cardDTO = productMapper.toProductCardDTO(product);
        cardDTO.setProductGroupedOptions(productLineService.getProductGroupedOptions(product.getProductLine().getId()));
        cardDTO.setProductCategoryChain(productCategoryService.getProductParentCategoryChain(product.getId()));
        return cardDTO;
    }

}

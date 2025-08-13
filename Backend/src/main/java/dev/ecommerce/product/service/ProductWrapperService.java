package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.*;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.entity.ProductLine;
import dev.ecommerce.product.entity.ProductLineDescription;
import dev.ecommerce.product.entity.ProductLineMedia;
import dev.ecommerce.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductWrapperService {

    private final ProductLineService productLineService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    public ProductWrapperService(ProductLineService productLineService,
                                 ProductService productService,
                                 ProductCategoryService productCategoryService,
                                 ProductMapper productMapper,
                                 ProductRepository productRepository) {
        this.productLineService = productLineService;
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.productMapper = productMapper;
            this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductLineDTO getProductLineDTObyProductId(Long productId) {
        Product product = productService.findProductById(productId);
        ProductLine productLine = product.getProductLine();
        if (productLine == null) {
            ProductLineDTO empty = new ProductLineDTO(null, null, null, null);
            empty.setProductIdList(new Long[] { productId });
            return empty;
        } else {
            ProductLineDTO dto = productMapper.toProductLineDTO(productLine);
            dto.setProductIdList(productRepository.findAllIdByProductLineId(productLine.getId()).orElse(new Long[]{ productId }));
            return dto;
        }
    }

    @Transactional
    public List<Long> saveAllProductInfo(ProductLineDTO productLineDTO, List<ProductDTO> productDTOList, Map<String, MultipartFile> fileMap) {
        List<Long> savedIds = new ArrayList<>();
        Integer productLineId = null;
        if (productLineDTO != null) {
            productLineId = productLineService.saveProductLine(productLineDTO, fileMap);
            if (productLineId != null)
                savedIds.add(productLineId.longValue());
        }
        for (ProductDTO productDTO : productDTOList) {
            savedIds.add(productService.saveProduct(productDTO, productLineId, fileMap));
        }
        return savedIds;
    }

    @Transactional
    public List<Long> updateAllProductInfo(ProductLineDTO productLineDTO,
                                           List<ProductDTO> updatingProductDTOList,
                                           List<ProductDTO> newProductDTOList,
                                           Map<String, MultipartFile> fileMap) {
        List<Long> updatedIds = new ArrayList<>();
        Integer productLineId = null;
        if (productLineDTO != null) {
            if (productLineDTO.getId() == null) {
                // if product line info is given with no id - meaning new product line to a product
                // only apply to the first product given, hence must get from the product id list in product line dto
                productLineId = productLineService.saveProductLine(productLineDTO, fileMap);
                productService.updateProductProductLine(productLineId, productLineDTO.getProductIdList()[0]);
            }
            else
                productLineId = productLineService.updateProductLineInfo(productLineDTO, fileMap);
            updatedIds.add(productLineId.longValue());
        }
        if (updatingProductDTOList != null && !updatingProductDTOList.isEmpty())
            for (ProductDTO productDTO : updatingProductDTOList) {
                updatedIds.add(productService.updateProductInfo(productDTO, productLineId, fileMap));
            }
        if (newProductDTOList != null && !newProductDTOList.isEmpty())
            for (ProductDTO productDTO : newProductDTOList) {
                updatedIds.add(productService.saveProduct(productDTO, productLineId, fileMap));
            }
        return updatedIds;
    }

    @Transactional
    public void deleteAllProductInfo(Integer productLineId, List<Long> productIdList) {
        if (productLineId != null) {
            List<Product> products = productLineService.findProductLineById(productLineId).getProducts();
            for (Product product : products) {
                productService.deleteProductById(product.getId());
            }
            productLineService.deleteProductLineById(productLineId);
        } else {
            for (Long productId : productIdList)
                productService.deleteProductById(productId);
        }
    }

    @Transactional(readOnly = true)
    public ProductCardDTO getProductCardById(Long id) {
        Product product = productService.findProductById(id);
        ProductCardDTO cardDTO = productMapper.toProductCardDTO(product);
        if (product.getProductLine() != null) {

            List<ContentDTO> productLineMedia = new ArrayList<>();
            for (ProductLineMedia media : product.getProductLine().getMedia()) {
                productLineMedia.add(productMapper.mediaToContentDTO(media));
            }
            cardDTO.getMedia().addAll(0, productLineMedia);

            List<ContentDTO> productLineDescription = new ArrayList<>();
            for (ProductLineDescription description : product.getProductLine().getDescriptions()) {
                productLineDescription.add(productMapper.descriptionsToContentDTO(description));
            }
            cardDTO.getDescriptions().addAll(0, productLineDescription);

            cardDTO.setProductGroupedOptions(productLineService.getProductGroupedOptions(product.getProductLine().getId()));
        }
        cardDTO.setProductCategoryChain(productCategoryService.getProductParentCategoryChain(product.getId()));
        return cardDTO;
    }

}

package dev.ecommerce.product.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.ContentDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.entity.ProductLine;
import dev.ecommerce.product.entity.ProductLineDescription;
import dev.ecommerce.product.entity.ProductLineMedia;
import dev.ecommerce.product.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductLineService {

    private final ProductLineRepository productLineRepository;
    private final ProductLineMediaRepository productLineMediaRepository;
    private final ProductLineDescriptionRepository productLineDescriptionRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductMapper productMapper;

    public ProductLineService(
            ProductLineRepository productLineRepository,
            ProductLineMediaRepository productLineMediaRepository,
            ProductLineDescriptionRepository productLineDescriptionRepository,
            ProductRepository productRepository,
            ProductOptionRepository productOptionRepository,
            ProductMapper productMapper
    ) {
        this.productLineRepository = productLineRepository;
        this.productLineMediaRepository = productLineMediaRepository;
        this.productLineDescriptionRepository = productLineDescriptionRepository;
        this.productRepository = productRepository;
        this.productOptionRepository = productOptionRepository;
        this.productMapper = productMapper;
    }

    public void findProductGroupedOptions(Integer productLineId) {
        if (productLineId == null)
            throw new IllegalStateException("Passing null Product line id");
        List<ProductOptionGroupProjection> groupedOptions = productOptionRepository.findProductOptionByProductLine(productLineId);
    }

    private ProductLine getProductLineById(Integer id) {
        if (id == null)
            throw new IllegalArgumentException("Product line id is null");
        return productLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product line not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ProductLineDTO findProductLineById(Integer id) {
        ProductLine productLine = getProductLineById(id);

        productLine.getDescriptions().size();
        productLine.getMedia().size();

        ProductLineDTO productLineDTO = productMapper.toProductLineDTO(productLine);
        productLineDTO.setProductIdList(productRepository.findAllIdByProductLineId(id).orElse(null));

        return productLineDTO;
    }

    @Transactional
    public Integer saveProductLine(ProductLineDTO productLineDTO) {
        if (productLineDTO.getName() == null)
            throw new DataIntegrityViolationException("Product line name is null");
        ProductLine savedProductLine = productLineRepository.save(new ProductLine(productLineDTO.getName()));

        List<ProductLineMedia> mediaList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getMedia().size(); j++) {
            mediaList.add(new ProductLineMedia(
                    savedProductLine,
                    productLineDTO.getMedia().get(j).contentType(),
                    productLineDTO.getMedia().get(j).content(),
                    j
            ));
        }
        productLineMediaRepository.saveAll(mediaList);

        List<ProductLineDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getDescriptions().size(); j++) {
            descriptionList.add(new ProductLineDescription(
                    savedProductLine,
                    productLineDTO.getDescriptions().get(j).contentType(),
                    productLineDTO.getDescriptions().get(j).content(),
                    j
            ));
        }
        productLineDescriptionRepository.saveAll(descriptionList);
        return savedProductLine.getId();
    }

    @Transactional // will leverage entity manager to update by retrieving the entity itself
    public Integer updateProductLineInfo(int productLineId, ProductLineDTO productLineDTO) {
        ProductLine productLine = getProductLineById(productLineId);

        if (productLineDTO.getName().isEmpty())
            throw new IllegalArgumentException("Product line name is empty");

        if (!productLine.getName().equals(productLineDTO.getName()))
            productLine.setName(productLineDTO.getName());

        // update media
        Map<Long, ProductLineMedia> currentMediaMap = productLine.getMedia().stream() // to get existing media entity by id quickly
                .collect(Collectors.toMap(ProductLineMedia::getId, m -> m));
        List<ProductLineMedia> updatedMediaList = ProductService.buildUpdatedMediaList(
                productLineDTO.getMedia(),
                currentMediaMap,
                (dto, sortOrder) -> new ProductLineMedia(productLine, dto.contentType(), dto.content(), sortOrder)
        );
        productLine.getMedia().clear();
        productLine.getMedia().addAll(updatedMediaList);

        // update description
        Map<Long, ProductLineDescription> currentDescriptionMap = productLine.getDescriptions().stream()
                .collect(Collectors.toMap(ProductLineDescription::getId, m -> m));
        List<ProductLineDescription> updatedDescriptionList = ProductService.buildUpdatedMediaList(
                productLineDTO.getDescriptions(),
                currentDescriptionMap,
                (dto, sortOrder) -> new ProductLineDescription(productLine, dto.contentType(), dto.content(), sortOrder)
        );
        productLine.getDescriptions().clear();
        productLine.getDescriptions().addAll(updatedDescriptionList);

        return productLineRepository.save(productLine).getId();
    }

    @Transactional
    public void deleteProductLineById(Integer id) {
        ProductLine productLine = getProductLineById(id);
        productLine.getProducts().forEach(product -> product.setProductLine(null));
        productLineRepository.delete(productLine);
    }

}


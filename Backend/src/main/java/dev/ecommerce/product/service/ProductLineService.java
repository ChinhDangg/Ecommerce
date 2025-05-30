package dev.ecommerce.product.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.ContentDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.entity.ProductLine;
import dev.ecommerce.product.entity.ProductLineDescription;
import dev.ecommerce.product.entity.ProductLineMedia;
import dev.ecommerce.product.repository.ProductLineDescriptionRepository;
import dev.ecommerce.product.repository.ProductLineMediaRepository;
import dev.ecommerce.product.repository.ProductLineRepository;
import dev.ecommerce.product.repository.ProductRepository;
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
    private final ProductMapper productMapper;

    public ProductLineService(
            ProductLineRepository productLineRepository,
            ProductLineMediaRepository productLineMediaRepository,
            ProductLineDescriptionRepository productLineDescriptionRepository,
            ProductRepository productRepository,
            ProductMapper productMapper
    ) {
        this.productLineRepository = productLineRepository;
        this.productLineMediaRepository = productLineMediaRepository;
        this.productLineDescriptionRepository = productLineDescriptionRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public ProductLineDTO findProductLineById(Integer id) {
        ProductLine productLine = productLineRepository.findById(id).orElse(null);
        if (productLine == null)
            return null;
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
    public Integer updateProductLine(int productLineId, ProductLineDTO productLineDTO) {
        ProductLine productLine = productLineRepository.findById(productLineId)
                .orElseThrow(() -> new ResourceNotFoundException("Product line id not found"));

        productLine.setName(productLineDTO.getName());

        List<ProductLineMedia> currentMedia = productLine.getMedia();
        Map<Long, ProductLineMedia> currentMediaMap = currentMedia.stream() // to get existing media entity by id quickly
                .filter(m -> m.getId() != null)
                .collect(Collectors.toMap(ProductLineMedia::getId, m -> m));

        List<ProductLineMedia> updatedMediaList = new ArrayList<>();
        int order = 0;
        for (ContentDTO contentDTO : productLineDTO.getMedia()) {
            if (contentDTO.id() != null && currentMediaMap.containsKey(contentDTO.id())) {
                // update existing
                ProductLineMedia media = currentMediaMap.get(contentDTO.id());
                if (contentDTO.contentType() != (media.getContentType()))
                    media.setContentType(contentDTO.contentType());
                if (!contentDTO.content().equals(media.getContent()))
                    media.setContent(contentDTO.content());
                if (media.getSortOrder() != order)
                    media.setSortOrder(order);
                updatedMediaList.add(media);
            } else {
                // create new media
                ProductLineMedia newMedia = new ProductLineMedia(
                        productLine,
                        contentDTO.contentType(),
                        contentDTO.content(),
                        order
                );
                updatedMediaList.add(newMedia);
            }
            order++;
        }
        productLine.getMedia().clear();
        productLine.getMedia().addAll(updatedMediaList);

        List<ProductLineDescription> currentDescription = productLine.getDescriptions();
        Map<Long, ProductLineDescription> currentDescriptionMap = currentDescription.stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(ProductLineDescription::getId, m -> m));

        List<ProductLineDescription> updatedDescriptionList = new ArrayList<>();
        int desOrder = 0;
        for (ContentDTO contentDTO : productLineDTO.getDescriptions()) {
            if (contentDTO.id() != null && currentDescriptionMap.containsKey(contentDTO.id())) {
                ProductLineDescription description = currentDescriptionMap.get(contentDTO.id());
                // update existing
                if (contentDTO.contentType() != (description.getContentType()))
                    description.setContentType(contentDTO.contentType());
                if (!contentDTO.content().equals(description.getContent()))
                    description.setContent(contentDTO.content());
                if (description.getSortOrder() != desOrder)
                    description.setSortOrder(desOrder);
                updatedDescriptionList.add(description);
            } else {
                ProductLineDescription newDescription = new ProductLineDescription(
                        productLine,
                        contentDTO.contentType(),
                        contentDTO.content(),
                        desOrder
                );
                updatedDescriptionList.add(newDescription);
            }
            desOrder++;
        }
        productLine.getDescriptions().clear();
        productLine.getDescriptions().addAll(updatedDescriptionList);

        return productLineRepository.save(productLine).getId();
    }
}

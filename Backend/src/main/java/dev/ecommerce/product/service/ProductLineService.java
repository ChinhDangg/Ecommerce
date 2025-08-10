package dev.ecommerce.product.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.ContentDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.DTO.ProductOptionDTO;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final MediaService mediaService;

    public ProductLineService(
            ProductLineRepository productLineRepository,
            ProductLineMediaRepository productLineMediaRepository,
            ProductLineDescriptionRepository productLineDescriptionRepository,
            ProductRepository productRepository,
            ProductMapper productMapper,
            MediaService mediaService) {
        this.productLineRepository = productLineRepository;
        this.productLineMediaRepository = productLineMediaRepository;
        this.productLineDescriptionRepository = productLineDescriptionRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.mediaService = mediaService;
    }

    public ProductLine findProductLineById(Integer id) {
        if (id == null)
            throw new IllegalArgumentException("Product line id is null");
        return productLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product line not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductOptionDTO> getProductGroupedOptions(Integer productLineId) {
        ProductLine productLine = findProductLineById(productLineId);
        List<ProductOption> productOptions = productLine.getProductOptions();
        return productMapper.toProductOptionDTOList(productOptions);
    }

    @Transactional(readOnly = true)
    public ProductLineDTO getProductLineDTOById(Integer id) {
        ProductLine productLine = findProductLineById(id);

        productLine.getDescriptions().size();
        productLine.getMedia().size();

        ProductLineDTO productLineDTO = productMapper.toProductLineDTO(productLine);
        productLineDTO.setProductIdList(productRepository.findAllIdByProductLineId(id).orElse(null));

        return productLineDTO;
    }

    @Transactional
    public Integer saveProductLine(ProductLineDTO productLineDTO, Map<String, MultipartFile> fileMap) {
        if (productLineDTO.getName() == null)
            throw new DataIntegrityViolationException("Product line name is null");

        List<String> filenameList = new ArrayList<>();

        // unsaved media if transaction failed
        TransactionSynchronizationManager.registerSynchronization(mediaService.getMediaTransactionSynForSaving(filenameList));

        ProductLine savedProductLine = productLineRepository.save(new ProductLine(productLineDTO.getName()));

        List<ProductLineMedia> mediaList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getMedia().size(); j++) {
            ContentDTO mediaDTO = productLineDTO.getMedia().get(j);

            String mediaName = mediaService.checkAndSaveMediaFile(mediaDTO, fileMap.get(mediaDTO.content()));
            filenameList.add(mediaName);

            mediaList.add(new ProductLineMedia(savedProductLine, mediaDTO.contentType(), mediaName, j));
        }
        productLineMediaRepository.saveAll(mediaList);

        List<ProductLineDescription> descriptionList = new ArrayList<>();
        for (int j = 0; j < productLineDTO.getDescriptions().size(); j++) {
            ContentDTO descriptionDTO = productLineDTO.getDescriptions().get(j);

            String contentName = mediaService.checkAndSaveMediaFile(descriptionDTO, fileMap.get(descriptionDTO.content()));
            if (descriptionDTO.contentType().isMedia()) {
                filenameList.add(contentName);
            }

            descriptionList.add(new ProductLineDescription(savedProductLine, descriptionDTO.contentType(), contentName, j));
        }
        productLineDescriptionRepository.saveAll(descriptionList);
        return savedProductLine.getId();
    }

    @Transactional // will leverage entity manager to update by retrieving the entity itself
    public Integer updateProductLineInfo(ProductLineDTO productLineDTO, Map<String, MultipartFile> fileMap) {
        ProductLine productLine = findProductLineById(productLineDTO.getId());

        List<String> oldFilenames = new ArrayList<>();
        List<String> updatedFilenames = new ArrayList<>();

        TransactionSynchronizationManager.registerSynchronization(mediaService.getMediaTransactionSynForUpdating(
            oldFilenames, updatedFilenames, fileMap
        ));

        if (productLineDTO.getName().isEmpty())
            throw new IllegalArgumentException("Product line name is empty");

        if (!productLine.getName().equals(productLineDTO.getName()))
            productLine.setName(productLineDTO.getName());

        // update media
        List<ProductLineMedia> oldMedia = productLine.getMedia();
        oldFilenames.addAll(oldMedia.stream().map(ProductLineMedia::getContent).toList());
        Map<Long, ProductLineMedia> currentMediaMap = oldMedia
                .stream()
                .collect(Collectors.toMap(ProductLineMedia::getId, m -> m)); // to get existing media entity by id quickly
        List<ProductLineMedia> updatedMediaList = mediaService.buildUpdatedMediaList(
                productLineDTO.getMedia(),
                currentMediaMap,
                (dto, sortOrder) -> new ProductLineMedia(productLine, dto.contentType(), dto.content(), sortOrder)
        );
        updatedFilenames.addAll(updatedMediaList.stream().map(ProductLineMedia::getContent).toList());

        productLine.getMedia().clear();
        productLine.getMedia().addAll(updatedMediaList);

        // update description
        List<ProductLineDescription> oldDescription = productLine.getDescriptions();
        oldFilenames.addAll(oldDescription.stream()
                .filter(d -> d.getContentType().isMedia())
                .map(ProductLineDescription::getContent)
                .toList());
        Map<Long, ProductLineDescription> currentDescriptionMap = oldDescription
                .stream()
                .collect(Collectors.toMap(ProductLineDescription::getId, m -> m));
        List<ProductLineDescription> updatedDescriptionList = mediaService.buildUpdatedMediaList(
                productLineDTO.getDescriptions(),
                currentDescriptionMap,
                (dto, sortOrder) -> new ProductLineDescription(productLine, dto.contentType(), dto.content(), sortOrder)
        );
        updatedFilenames.addAll(updatedDescriptionList.stream()
                        .filter(d -> d.getContentType().isMedia())
                        .map(ProductLineDescription::getContent)
                        .toList());

        productLine.getDescriptions().clear();
        productLine.getDescriptions().addAll(updatedDescriptionList);

        try {
            mediaService.updateSavedImageToTemp(oldFilenames, updatedFilenames, fileMap);
        } catch (IOException e) {
            throw new RuntimeException("Fail to update media");
        }

        return productLineRepository.save(productLine).getId();
    }

    @Transactional
    public void deleteProductLineById(Integer id) {
        ProductLine productLine = findProductLineById(id);
        productLine.getProducts().forEach(product -> product.setProductLine(null));
        productLine.getProductOptions().forEach(option -> option.setProductLine(null));
        productLineRepository.delete(productLine);
    }

}


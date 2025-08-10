package dev.ecommerce.product.service;

import dev.ecommerce.product.DTO.ContentDTO;
import dev.ecommerce.product.constant.ContentType;
import dev.ecommerce.product.entity.BaseContent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
public class MediaService {

    private final String IMAGE_DIR = "uploads/images";
    private final String TEMP_IMAGE_DIR = "tmp/images";

    public Resource getImageResource(String filename) throws MalformedURLException {
        Path filePath = Paths.get(IMAGE_DIR).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists())
            return null;
        return resource;
    }

    public String saveImage(MultipartFile file, boolean isTemp) {
        try {
            // Save file to a directory
            String dir = isTemp ? TEMP_IMAGE_DIR : IMAGE_DIR;
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(dir, fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return path.getFileName().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    public void saveImages(List<String> filenames, Map<String, MultipartFile> fileMap, boolean isTemp) {
        for (String filename : filenames) {
            if (!fileMap.containsKey(filename))
                throw new IllegalArgumentException("File not found with name: " + filename);
            saveImage(fileMap.get(filename), isTemp);
        }
    }

    public void moveTempToPermanent(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            Path tempPath = Paths.get(TEMP_IMAGE_DIR).resolve(filename).normalize();
            Path finalPath = Paths.get(IMAGE_DIR).resolve(filename).normalize();
            Files.move(tempPath, finalPath);
        }
    }

    public void deleteFromTemp(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            Path tempPath = Paths.get(TEMP_IMAGE_DIR).resolve(filename).normalize();
            Files.deleteIfExists(tempPath);
        }
    }

    public void deleteFromPermanent(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            Path tempPath = Paths.get(IMAGE_DIR).resolve(filename).normalize();
            Files.deleteIfExists(tempPath);
        }
    }

    public TransactionSynchronization getMediaTransactionSynForSaving(List<String> filenames) {
        return new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        // Move to the final directory
                        moveTempToPermanent(filenames);
                        System.out.println("File committed: " + filenames);
                    } else {
                        // Rollback → delete temp file
                        deleteFromTemp(filenames);
                        System.out.println("File deleted due to rollback: " + filenames);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public TransactionSynchronization getMediaTransactionSynForUpdating(List<String> oldFilenames,
                                                                        List<String> updatedFilenames,
                                                                        Map<String, MultipartFile> fileMap) {
        return new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    List<String> markForDeleteFiles = oldFilenames.stream().filter(f -> !updatedFilenames.contains(f)).toList();
                    List<String> markForAddFiles = updatedFilenames.stream().filter(f -> !oldFilenames.contains(f)).toList();
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        // Move to the final directory
                        moveTempToPermanent(markForAddFiles);
                        System.out.println("File committed: " + markForAddFiles);
                    } else {
                        // Rollback → delete temp file
                        deleteFromTemp(markForAddFiles);
                        // undo deletion
                        saveImages(markForDeleteFiles, fileMap, false);
                        System.out.println("File deleted due to rollback: " + markForAddFiles + "\nand restored from permanent: " + markForDeleteFiles);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * check given mediaDTO has IMAGE or VIDEO as ContentType,
     * if true, then save the media and return the saved name based on the content name,
     * else, throw error as contentDTO is a media type but no file is given.
     * if ContentType is not a media type, then just return the contentDTO content name.
     */
    public String checkAndSaveMediaFile(ContentDTO mediaDTO, MultipartFile file) {
        if (mediaDTO.contentType().isMedia() && file == null) // saving means completely new media, expected all media given to have an association with multipartFile list
            throw new IllegalArgumentException("File not found with name: " + mediaDTO.content());

        return (mediaDTO.contentType() == ContentType.IMAGE) ? saveImage(file, true) : mediaDTO.content();
    }

    /**
     * Compare the old list to the new list.
     * Any missing from the new list will be removed from permanent.
     * Any not from the old list will be saved to temp
     */
    public void updateSavedImageToTemp(List<String> oldFilenames, List<String> updatedFilenames, Map<String, MultipartFile> fileMap) throws IOException {
        List<String> deletedFilenames = oldFilenames.stream().filter(f -> !updatedFilenames.contains(f)).toList();
        List<String> addedFilenames = updatedFilenames.stream().filter(f -> !oldFilenames.contains(f)).toList();
        deleteFromPermanent(deletedFilenames);
        saveImages(addedFilenames, fileMap, true);
    }

    public <T extends BaseContent> List<T> buildUpdatedMediaList(
            List<ContentDTO> incomingDTOs,
            Map<Long, T> currentMediaMap,
            BiFunction<ContentDTO, Integer, T> newMediaFactory
    ) {
        List<T> updatedList = new ArrayList<>();
        int order = 0;
        for (ContentDTO dto : incomingDTOs) {
            if (dto.id() != null && currentMediaMap.containsKey(dto.id())) {
                T media = currentMediaMap.get(dto.id());
                if (dto.contentType() != media.getContentType())
                    media.setContentType(dto.contentType());
                if (!dto.content().equals(media.getContent()))
                    media.setContent(dto.content());
                if (media.getSortOrder() != order)
                    media.setSortOrder(order);
                updatedList.add(media);
            } else {
                T newMedia = newMediaFactory.apply(dto, order);
                updatedList.add(newMedia);
            }
            order++;
        }
        return updatedList;
    }
}

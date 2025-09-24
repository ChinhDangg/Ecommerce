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

    public String saveMedia(MultipartFile file, boolean isTemp) {
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

    public List<String> saveMedia(List<String> filenames, Map<String, MultipartFile> fileMap, boolean isTemp) {
        List<String> savedFilenames = new ArrayList<>();
        for (String filename : filenames) {
            if (!fileMap.containsKey(filename))
                throw new IllegalArgumentException("File not found with name: " + filename);
            savedFilenames.add(saveMedia(fileMap.get(filename), isTemp));
        }
        return savedFilenames;
    }

    public void moveTempToPermanent(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            Path tempPath = Paths.get(TEMP_IMAGE_DIR).resolve(filename).normalize();
            Path finalPath = Paths.get(IMAGE_DIR).resolve(filename).normalize();
            Files.createDirectories(finalPath.getParent()); // create the permanent path if not exist
            Files.move(tempPath, finalPath);
        }
    }

    public void movePermanentToTemp(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            Path tempPath = Paths.get(TEMP_IMAGE_DIR).resolve(filename).normalize();
            Path finalPath = Paths.get(IMAGE_DIR).resolve(filename).normalize();
            Files.createDirectories(tempPath.getParent()); // create the temp path if not exist
            Files.move(finalPath, tempPath);
        }
    }

    private void deleteFromTemp(List<String> filenames) throws IOException {
        for (String filename : filenames) {
            Path tempPath = Paths.get(TEMP_IMAGE_DIR).resolve(filename).normalize();
            Files.deleteIfExists(tempPath);
        }
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

        return (mediaDTO.contentType() == ContentType.IMAGE) ? saveMedia(file, true) : mediaDTO.content();
    }

    /**
     * Requires saving files to temp first.
     * Files in temp will be moved to permanent upon committed.
     * Else on rollback, delete files from temp.
     */
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

    /**
     * Requires files marked as deletion be in temp and files marked as adding in temp.
     * Marked as deletion files will be deleted in temp upon committed.
     * Marked as adding files will move from temp to permanent upon committed
     * Else on rollback, marked as deletion files in temp will be moved back to permanent.
     * Else on rollback, marked as adding files in temp will be deleted.
     */
    public TransactionSynchronization getMediaTransactionSynForUpdating(List<String> markForDeleteFiles,
                                                                        List<String> markForAddFiles) {
        return new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        // Move to the final directory
                        moveTempToPermanent(markForAddFiles);
                        deleteFromTemp(markForDeleteFiles);
                        System.out.println("File committed: " + markForAddFiles);
                    } else {
                        // Rollback → delete temp file
                        deleteFromTemp(markForAddFiles);
                        // undo deletion
                        moveTempToPermanent(markForDeleteFiles);
                        System.out.println("File deleted due to rollback: " + markForAddFiles + "\nand restored from permanent: " + markForDeleteFiles);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Requires moving files from permanent to tmp for files to be deleted.
     * Files in temp will be deleted upon committed.
     * Else on rollback, move files from temp back to permanent.
     */
    public TransactionSynchronization getMediaTransactionSynForDeleting(List<String> filenames) {
        return new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                try {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        // delete from tmp
                        deleteFromTemp(filenames);
                        System.out.println("File committed: " + filenames);
                    } else {
                        // Rollback → undo deletion by moving back from tmp to permanent
                        moveTempToPermanent(filenames);
                        System.out.println("File deleted due to rollback: " + filenames);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
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

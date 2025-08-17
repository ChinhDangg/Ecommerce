package dev.ecommerce.product.controller;

import dev.ecommerce.product.service.MediaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media")
public class ImageController {

    private final MediaService mediaService;

    public ImageController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename, HttpServletRequest request) {
        try {
            Resource resource = mediaService.getImageResource(filename);

            if (!resource.exists()) {
                System.out.println("File not found: " + filename);
                return ResponseEntity.notFound().build();
            }

            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}

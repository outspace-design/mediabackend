package com.sribalajiads.media_app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads")
public class ImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(determineContentType(filename))
                        .body(resource);
            }

            String baseName = getBaseName(filename);
            String[] extensions = {".jpg", ".jpeg", ".png", ".webp", ".JPG", ".JPEG", ".PNG"};

            for (String ext : extensions) {
                Path altFile = Paths.get(uploadDir).resolve(baseName + ext);
                Resource altResource = new UrlResource(altFile.toUri());
                if (altResource.exists() || altResource.isReadable()) {
                    return ResponseEntity.ok()
                            .contentType(determineContentType(baseName + ext))
                            .body(altResource);
                }
            }

            return ResponseEntity.notFound().build();

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getBaseName(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }

    private MediaType determineContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}

package com.sribalajiads.media_app.service;

import com.sribalajiads.media_app.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage location", e);
        }
    }

    /**
     * Stores an uploaded file securely using a custom filename.
     * @param file The file uploaded by the user.
     * @param desiredFilename The base name for the file (without extension).
     * @return The full filename with extension.
     */
    public String store(MultipartFile file, String desiredFilename) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String finalFilename = desiredFilename + "." + fileExtension;

        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file " + originalFilename);
            }
            if (finalFilename.contains("..")) {
                throw new FileStorageException("Cannot store file with relative path outside current directory " + finalFilename);
            }

            Path destinationFile = this.rootLocation.resolve(Paths.get(finalFilename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return finalFilename;

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + finalFilename, e);
        }
    }

    /**
     * Stores raw bytes as a file using a custom filename and extension.
     * Used for bulk zip uploads where we have bytes directly.
     * @param data The raw file bytes.
     * @param desiredFilename The base name for the file (without extension).
     * @param extension The file extension (e.g., "jpg", "png").
     * @return The full filename with extension.
     */
    public String store(byte[] data, String desiredFilename, String extension) {
        String finalFilename = desiredFilename + "." + extension;

        try {
            if (data == null || data.length == 0) {
                throw new FileStorageException("Failed to store empty file data for " + finalFilename);
            }
            if (finalFilename.contains("..")) {
                throw new FileStorageException("Cannot store file with relative path outside current directory " + finalFilename);
            }

            Path destinationFile = this.rootLocation.resolve(Paths.get(finalFilename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return finalFilename;

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + finalFilename, e);
        }
    }

    public String rename(String oldFilename, String newFilenameBase) {
        Path oldFilePath = this.rootLocation.resolve(oldFilename).normalize().toAbsolutePath();

        if (!Files.exists(oldFilePath)) {
            throw new FileStorageException("Cannot rename file. Source file not found: " + oldFilename);
        }

        String fileExtension = StringUtils.getFilenameExtension(oldFilename);
        String finalNewFilename = newFilenameBase + "." + fileExtension;
        Path newFilePath = this.rootLocation.resolve(finalNewFilename).normalize().toAbsolutePath();

        try {
            Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            return finalNewFilename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to rename file from " + oldFilename + " to " + finalNewFilename, e);
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isBlank()) return;

        try {
            Path fileToDelete = rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file " + filename, e);
        }
    }
}

package org.example.banksproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageRoot;
    private final long maxFileSize = 10L * 1024L * 1024L; // 10 MB

    public FileStorageService(@Value("${attachments.storage.path:./data/attachments}") String storagePath) throws IOException {
        storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(storageRoot);
    }

    public record StoredFile(String filename, String contentType, long size, String path) {
    }

    public StoredFile store(MultipartFile file, int bankId, int mortgageId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename() == null ? "file.pdf" : file.getOriginalFilename();

        if (contentType == null || (!contentType.equalsIgnoreCase("application/pdf") && !originalFilename.toLowerCase().endsWith(".pdf"))) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            String headerStr = read > 0 ? new String(header, 0, Math.max(0, read)) : "";
            if (!headerStr.startsWith("%PDF")) {
                throw new IllegalArgumentException("File is not a valid PDF (magic bytes mismatch)");
            }
        }

        String safeOriginal = Paths.get(originalFilename).getFileName().toString();
        String unique = UUID.randomUUID().toString();
        // for each attachment the path will be: .../bankId/mortgageId/file.pdf
        Path targetDir = storageRoot.resolve(String.valueOf(bankId)).resolve(String.valueOf(mortgageId));
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(unique + "-" + safeOriginal);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return new StoredFile(safeOriginal, contentType, file.getSize(), target.toAbsolutePath().toString());
    }

    public Resource loadAsResource(String path) throws IOException {
        Path p = Paths.get(path);
        Resource resource = new UrlResource(p.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        }
        throw new IOException("Could not read file: " + path);
    }

    public void delete(String path) throws IOException {
        Path p = Paths.get(path);
        Files.deleteIfExists(p);
    }
}


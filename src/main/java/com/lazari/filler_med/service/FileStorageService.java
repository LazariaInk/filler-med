package com.lazari.filler_med.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public List<String> saveProductImages(Long productId, List<MultipartFile> files) {
        if (files == null) return List.of();
        List<String> urls = new ArrayList<>();
        Path base = Paths.get(uploadDir, "products", String.valueOf(productId));
        try { Files.createDirectories(base); } catch (Exception ignored) {}
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;

            String filename = UUID.randomUUID() + ".webp";
            Path target = base.resolve(filename);

            try {
                Thumbnails.of(f.getInputStream())
                        .size(1600, 1600)
                        .outputFormat("webp")
                        .outputQuality(0.85)
                        .toFile(target.toFile());

                urls.add("/media/products/" + productId + "/" + filename);
            } catch (Exception e) {
                throw new RuntimeException("Nu pot procesa imaginea: " + f.getOriginalFilename(), e);
            }
        }
        return urls;
    }

    public void deleteByMediaUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) return;
        String normalized = mediaUrl.startsWith("/") ? mediaUrl.substring(1) : mediaUrl;
        Path filePath = Paths.get(uploadDir).resolve(normalized.replaceFirst("^media/", ""));
        try {
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            System.err.println("Nu am putut șterge fișierul: " + filePath + " | " + e.getMessage());
        }
    }

}

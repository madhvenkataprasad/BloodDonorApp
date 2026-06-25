package com.blooddonor.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "pdf");

    private final Cloudinary cloudinary;

    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String store(MultipartFile file, String subFolder) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String ext = extension(file.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException(
                    "Only JPG, PNG and PDF files are allowed"
            );
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", subFolder,
                        "resource_type", "auto"
                )
        );

        return uploadResult.get("secure_url").toString();
    }

    public String resolve(String storedFileName) {
        return storedFileName;
    }

    private String extension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(
                filename.lastIndexOf('.') + 1
        ).toLowerCase();
    }
}
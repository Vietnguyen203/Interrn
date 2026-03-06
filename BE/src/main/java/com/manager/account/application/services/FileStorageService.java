package com.manager.account.application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final String baseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTS  = Set.of("jpg", "jpeg", "png", "webp");

    public FileStorageService(
            @Value("${file.upload-dir:uploads}") String uploadDir, // ✅ mặc định "uploads"
            @Value("${file.base-url:http://localhost:9720/foodordersystem/api/uploads/}") String baseUrl // ✅ fallback
    ) throws IOException {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    /** Lưu file và trả về URL đầy đủ để truy cập. */
    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File rỗng");
        }

        // Kiểm tra content-type (nếu null thì dựa vào extension)
        String contentType = file.getContentType();
        String originalExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String ext = originalExtension != null ? originalExtension.toLowerCase() : null;

        if (contentType != null) {
            if (!ALLOWED_TYPES.contains(contentType)) {
                throw new IOException("File không hợp lệ. Chỉ chấp nhận jpg, png, webp.");
            }
        } else {
            if (ext == null || !ALLOWED_EXTS.contains(ext)) {
                throw new IOException("File không hợp lệ. Chỉ chấp nhận jpg, png, webp.");
            }
        }

        // Tạo tên file random (UUID + ext nếu có)
        String fileName = UUID.randomUUID() + (ext != null ? "." + ext : "");

        // Lưu file
        Path targetLocation = this.fileStorageLocation.resolve(fileName).normalize();
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Trả URL tuyệt đối để FE dùng
        return baseUrl + fileName;
    }
}





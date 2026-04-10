package com.manager.catalog.interfaces.rest.api;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.catalog.application.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadAPI {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public BaseResponseDTO uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // FileStorageService đã trả về URL đầy đủ (baseUrl + fileName)
            String fileUrl = fileStorageService.storeFile(file);
            return new BaseResponseDTO("OK", "Success", Map.of("url", fileUrl));
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", "Lỗi tải tệp: " + e.getMessage());
        }
    }
}

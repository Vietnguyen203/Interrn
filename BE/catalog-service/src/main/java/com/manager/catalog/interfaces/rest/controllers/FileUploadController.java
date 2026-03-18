package com.manager.catalog.interfaces.rest.controllers;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.catalog.application.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public BaseResponseDTO uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // FileStorageService đã trả về URL đầy đủ (baseUrl + fileName)
            String fileUrl = fileStorageService.storeFile(file);

            return new BaseResponseDTO("OK", "Success", Map.of("url", fileUrl));
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", e.getMessage());
        }
    }
}

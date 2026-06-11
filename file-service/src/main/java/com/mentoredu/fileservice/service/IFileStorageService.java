package com.mentoredu.fileservice.service;

import com.mentoredu.fileservice.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {
    FileUploadResponse store(MultipartFile file, String folder);
    byte[] fetch(String fileUrl);
}

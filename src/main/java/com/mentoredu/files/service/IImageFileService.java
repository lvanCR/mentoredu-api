package com.mentoredu.files.service;

import com.mentoredu.files.dto.ImageFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IImageFileService {
    ImageFileResponse upload(MultipartFile file);
}

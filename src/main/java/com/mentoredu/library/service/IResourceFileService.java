package com.mentoredu.library.service;

import com.mentoredu.library.dto.ResourceFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IResourceFileService {
    ResourceFileResponse upload(MultipartFile file);
}

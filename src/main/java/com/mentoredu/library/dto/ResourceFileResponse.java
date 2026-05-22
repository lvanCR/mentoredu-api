package com.mentoredu.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceFileResponse {
    private final String fileUrl;
    private final String fileName;
    private final String mimeType;
    private final Long   sizeBytes;
}

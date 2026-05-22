package com.mentoredu.library.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DownloadResponse {
    private UUID resourceId;
    private String title;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long sizeBytes;
}

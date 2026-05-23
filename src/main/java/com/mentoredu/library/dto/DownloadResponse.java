package com.mentoredu.library.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DownloadResponse(UUID resourceId, String title, String fileUrl, String fileName, String mimeType, Long sizeBytes) {}

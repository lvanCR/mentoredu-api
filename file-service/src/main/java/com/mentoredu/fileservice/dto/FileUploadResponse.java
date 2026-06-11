package com.mentoredu.fileservice.dto;

public record FileUploadResponse(String fileUrl, String fileName, String mimeType, Long sizeBytes) {}

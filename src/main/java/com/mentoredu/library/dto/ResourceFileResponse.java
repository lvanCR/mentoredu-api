package com.mentoredu.library.dto;

import com.mentoredu.library.model.ResourceFile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ResourceFileResponse {

    private final UUID          id;
    private final String        fileUrl;
    private final String        fileName;
    private final String        mimeType;
    private final Long          sizeBytes;
    private final LocalDateTime createdAt;

    public ResourceFileResponse(ResourceFile entity) {
        this.id        = entity.getId();
        this.fileUrl   = entity.getFileUrl();
        this.fileName  = entity.getFileName();
        this.mimeType  = entity.getMimeType();
        this.sizeBytes = entity.getSizeBytes();
        this.createdAt = entity.getCreatedAt();
    }
}

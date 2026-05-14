package com.mentoredu.content.dto;

import com.mentoredu.content.model.AcademicResource;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DocumentResponse {

    private final UUID id;
    private final String title;
    private final String type;
    private final String category;
    private final String fileUrl;
    private final String fileName;
    private final String contentType;
    private final Long fileSize;
    private final Integer version;
    private final String university;
    private final Integer year;
    private final String area;
    private final Boolean verified;
    private final Boolean anonymous;
    private final LocalDateTime createdAt;
    private final String authorName;

    public DocumentResponse(AcademicResource resource) {
        this.id = resource.getId();
        this.title = resource.getTitle();
        this.type = resource.getType();
        this.category = resource.getCategory();
        this.fileUrl = resource.getFileUrl();
        this.fileName = resource.getFileName();
        this.contentType = resource.getContentType();
        this.fileSize = resource.getFileSize();
        this.version = resource.getVersion();
        this.university = resource.getUniversity();
        this.year = resource.getYear();
        this.area = resource.getArea();
        this.verified = "VERIFIED".equals(resource.getVerificationStatus());
        this.anonymous = resource.getAnonymous();
        this.createdAt = resource.getCreatedAt();
        this.authorName = Boolean.TRUE.equals(resource.getAnonymous())
                ? "Usuario Anónimo"
                : resource.getAuthor().getFirstName() + " " + resource.getAuthor().getLastName();
    }
}

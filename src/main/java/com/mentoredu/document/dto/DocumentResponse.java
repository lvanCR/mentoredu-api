package com.mentoredu.document.dto;

import com.mentoredu.document.model.Document;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DocumentResponse {

    private final Long id;
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

    public DocumentResponse(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.type = document.getType();
        this.category = document.getCategory();
        this.fileUrl = document.getFileUrl();
        this.fileName = document.getFileName();
        this.contentType = document.getContentType();
        this.fileSize = document.getFileSize();
        this.version = document.getVersion();
        this.university = document.getUniversity();
        this.year = document.getYear();
        this.area = document.getArea();
        this.verified = document.getVerified();
        this.anonymous = document.getAnonymous();
        this.createdAt = document.getCreatedAt();
        this.authorName = Boolean.TRUE.equals(document.getAnonymous())
                ? "Usuario Anónimo"
                : document.getAuthor().getFirstName() + " " + document.getAuthor().getLastName();
    }
}

package com.mentoredu.library.dto;

import com.mentoredu.library.model.Resource;
import com.mentoredu.library.model.ResourceType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ResourceResponse {

    private final UUID         id;
    private final String       title;
    private final ResourceType resourceType;
    private final String       description;
    private final UUID         universityId;
    private final UUID         areaId;
    private final UUID         careerId;
    private final UUID         courseId;
    private final String       fileUrl;
    private final String       fileName;
    private final String       mimeType;
    private final Long         sizeBytes;
    private final boolean      aceptaResoluciones;
    private final LocalDateTime createdAt;
    private final UUID                authorId;
    private final String              authorName;
    private final SubmissionStatusDto mySubmission;

    public ResourceResponse(Resource resource) {
        this(resource, null);
    }

    public ResourceResponse(Resource resource, SubmissionStatusDto mySubmission) {
        this.id                 = resource.getId();
        this.title              = resource.getTitle();
        this.resourceType       = resource.getResourceType();
        this.description        = resource.getDescription();
        this.universityId       = resource.getUniversityId();
        this.areaId             = resource.getAreaId();
        this.careerId           = resource.getCareerId();
        this.courseId           = resource.getCourseId();
        this.fileUrl            = resource.getFileUrl();
        this.fileName           = resource.getFileName();
        this.mimeType           = resource.getMimeType();
        this.sizeBytes          = resource.getSizeBytes();
        this.aceptaResoluciones = resource.isAceptaResoluciones();
        this.createdAt          = resource.getCreatedAt();
        this.authorId           = resource.getAuthor().getId();
        this.authorName         = resource.getAuthor().getFirstName() + " " + resource.getAuthor().getLastName();
        this.mySubmission       = mySubmission;
    }
}

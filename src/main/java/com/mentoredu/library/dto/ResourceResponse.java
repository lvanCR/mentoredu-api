package com.mentoredu.library.dto;

import com.mentoredu.library.model.AcademicResource;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ResourceResponse {

    private final UUID id;
    private final String title;
    private final String type;
    private final String visibility;
    private final String description;
    private final UUID subjectId;
    private final UUID institutionId;
    private final UUID fileId;
    private final Integer year;
    private final String examCycle;
    private final LocalDateTime createdAt;
    private final String authorName;

    public ResourceResponse(AcademicResource resource) {
        this.id = resource.getId();
        this.title = resource.getTitle();
        this.type = resource.getType();
        this.visibility = resource.getVisibility();
        this.description = resource.getDescription();
        this.subjectId = resource.getSubjectId();
        this.institutionId = resource.getInstitutionId();
        this.fileId = resource.getFileId();
        this.year = resource.getYear();
        this.examCycle = resource.getExamCycle();
        this.createdAt = resource.getCreatedAt();
        this.authorName = resource.getAuthor().getFirstName() + " " + resource.getAuthor().getLastName();
    }
}

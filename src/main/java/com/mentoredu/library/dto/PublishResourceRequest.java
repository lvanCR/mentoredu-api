package com.mentoredu.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class PublishResourceRequest {
    @NotBlank
    private String title;
    private String type;
    private String visibility;
    private String description;
    private UUID subjectId;
    private UUID institutionId;
    private Integer year;
    private String examCycle;
}

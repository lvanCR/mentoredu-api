package com.mentoredu.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class PublishResourceRequest {

    @NotBlank
    private String title;

    @NotNull
    private UUID fileId;

    @NotNull
    private UUID institutionId;

    @NotNull
    private UUID subjectId;

    @NotNull
    @Min(value = 1900, message = "examYear must be 1900 or later")
    @Max(value = 2099, message = "examYear must be 2099 or earlier")
    private Integer year;

    @NotBlank
    @Pattern(
        regexp = "EXAM|SOLUTION|NOTES|PRACTICE|VIDEO|OTHER",
        message = "resourceType must be one of: EXAM, SOLUTION, NOTES, PRACTICE, VIDEO, OTHER"
    )
    private String type;

    @Pattern(
        regexp = "PUBLIC|PREMIUM|PRIVATE",
        message = "visibility must be one of: PUBLIC, PREMIUM, PRIVATE"
    )
    private String visibility;

    private String description;

    private String examCycle;

    private Boolean allowsSolutions;
}

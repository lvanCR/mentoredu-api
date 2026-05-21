package com.mentoredu.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProgramRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must not exceed 120 characters")
    private String name;

    @NotBlank(message = "modality is required")
    @Size(max = 30, message = "modality must not exceed 30 characters")
    private String modality;

    @NotBlank(message = "intensity is required")
    @Size(max = 30, message = "intensity must not exceed 30 characters")
    private String intensity;

    @NotBlank(message = "targetUniversity is required")
    @Size(max = 120, message = "targetUniversity must not exceed 120 characters")
    private String targetUniversity;
}

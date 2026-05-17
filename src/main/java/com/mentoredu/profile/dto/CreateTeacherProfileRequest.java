package com.mentoredu.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTeacherProfileRequest {

    @NotBlank(message = "specialty is required")
    @Size(max = 120, message = "specialty must not exceed 120 characters")
    private String specialty;

    @NotBlank(message = "institutionName is required")
    @Size(max = 120, message = "institutionName must not exceed 120 characters")
    private String institutionName;

    @Size(max = 2000, message = "bioProfessional must not exceed 2000 characters")
    private String bioProfessional;
}

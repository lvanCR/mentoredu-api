package com.mentoredu.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateStudentProfileRequest {

    @NotBlank(message = "gradeLevel is required")
    @Size(max = 20, message = "gradeLevel must not exceed 20 characters")
    private String gradeLevel;

    @Size(max = 120, message = "schoolName must not exceed 120 characters")
    private String schoolName;

    @Size(max = 30, message = "studyShift must not exceed 30 characters")
    private String studyShift;

    private UUID targetUniversityId;
    private UUID targetAreaId;
    private UUID targetCareerId;
}

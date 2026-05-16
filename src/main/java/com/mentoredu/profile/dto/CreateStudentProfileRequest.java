package com.mentoredu.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStudentProfileRequest {

    @NotBlank(message = "gradeLevel is required")
    @Size(max = 20, message = "gradeLevel must not exceed 20 characters")
    private String gradeLevel;

    @NotBlank(message = "targetUniversity is required")
    @Size(max = 120, message = "targetUniversity must not exceed 120 characters")
    private String targetUniversity;

    @Size(max = 120, message = "schoolName must not exceed 120 characters")
    private String schoolName;

    @Size(max = 120, message = "targetCareer must not exceed 120 characters")
    private String targetCareer;

    @Size(max = 30, message = "studyShift must not exceed 30 characters")
    private String studyShift;
}

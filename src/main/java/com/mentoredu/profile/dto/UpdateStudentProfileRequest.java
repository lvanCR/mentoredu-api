package com.mentoredu.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStudentProfileRequest {
    @Size(max = 120)
    private String schoolName;
    @Size(max = 20)
    private String gradeLevel;
    @NotBlank(message = "targetUniversity is required")
    @Size(max = 120, message = "targetUniversity must not exceed 120 characters")
    private String targetUniversity;
    @Size(max = 120)
    private String targetCareer;
    @Size(max = 30)
    private String studyShift;
}

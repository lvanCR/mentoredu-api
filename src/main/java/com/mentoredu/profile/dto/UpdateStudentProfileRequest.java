package com.mentoredu.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateStudentProfileRequest {

    @Size(max = 120)
    private String schoolName;

    @Size(max = 20)
    private String gradeLevel;

    @Size(max = 30)
    private String studyShift;

    private UUID targetUniversityId;
    private UUID targetAreaId;
    private UUID targetCareerId;
}

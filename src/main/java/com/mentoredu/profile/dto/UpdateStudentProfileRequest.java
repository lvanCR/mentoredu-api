package com.mentoredu.profile.dto;

import lombok.Data;

@Data
public class UpdateStudentProfileRequest {
    private String schoolName;
    private String gradeLevel;
    private String targetUniversity;
    private String targetCareer;
    private String studyShift;
}

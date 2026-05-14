package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.StudentProfile;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StudentProfileResponse {

    private final UUID userId;
    private final String schoolName;
    private final String gradeLevel;
    private final String targetUniversity;
    private final String targetCareer;
    private final String studyShift;
    private final Boolean isAnonymousAllowed;

    public StudentProfileResponse(StudentProfile profile) {
        this.userId = profile.getUserId();
        this.schoolName = profile.getSchoolName();
        this.gradeLevel = profile.getGradeLevel();
        this.targetUniversity = profile.getTargetUniversity();
        this.targetCareer = profile.getTargetCareer();
        this.studyShift = profile.getStudyShift();
        this.isAnonymousAllowed = profile.getIsAnonymousAllowed();
    }
}

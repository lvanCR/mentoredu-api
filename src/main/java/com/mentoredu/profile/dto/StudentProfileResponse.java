package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.StudentProfile;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StudentProfileResponse {

    private final UUID profileId;
    private final String schoolName;
    private final String gradeLevel;
    private final String studyShift;
    private final UUID targetUniversityId;
    private final UUID targetAreaId;
    private final UUID targetCareerId;

    public StudentProfileResponse(StudentProfile profile) {
        this.profileId          = profile.getProfileId();
        this.schoolName         = profile.getSchoolName();
        this.gradeLevel         = profile.getGradeLevel();
        this.studyShift         = profile.getStudyShift();
        this.targetUniversityId = profile.getTargetUniversityId();
        this.targetAreaId       = profile.getTargetAreaId();
        this.targetCareerId     = profile.getTargetCareerId();
    }
}

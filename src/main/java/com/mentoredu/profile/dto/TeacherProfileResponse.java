package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.TeacherProfile;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TeacherProfileResponse {

    private final UUID profileId;
    private final String bioProfessional;
    private final String universities;
    private final String specialty;
    private final String courses;
    private final String experience;
    private final String summary;

    public TeacherProfileResponse(TeacherProfile profile) {
        this.profileId       = profile.getProfileId();
        this.bioProfessional = profile.getBioProfessional();
        this.universities    = profile.getUniversities();
        this.specialty       = profile.getSpecialty();
        this.courses         = profile.getCourses();
        this.experience      = profile.getExperience();
        this.summary         = profile.getSummary();
    }
}

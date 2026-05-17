package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.TeacherProfile;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TeacherProfileResponse {

    private final UUID profileId;
    private final String specialty;
    private final String institutionName;
    private final String bioProfessional;

    public TeacherProfileResponse(TeacherProfile profile) {
        this.profileId      = profile.getProfileId();
        this.specialty      = profile.getSpecialty();
        this.institutionName = profile.getInstitutionName();
        this.bioProfessional = profile.getBioProfessional();
    }
}

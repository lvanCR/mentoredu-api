package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.TeacherProfile;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TeacherProfileResponse {

    private final UUID profileId;
    private final String bioProfessional;

    public TeacherProfileResponse(TeacherProfile profile) {
        this.profileId       = profile.getProfileId();
        this.bioProfessional = profile.getBioProfessional();
    }
}

package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.Profile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ProfileResponse {

    private final UUID id;
    private final UUID userId;
    private final String displayName;
    private final String profileType;
    private final LocalDateTime createdAt;

    public ProfileResponse(Profile profile) {
        this.id          = profile.getId();
        this.userId      = profile.getUserId();
        this.displayName = profile.getDisplayName();
        this.profileType = profile.getProfileType();
        this.createdAt   = profile.getCreatedAt();
    }
}

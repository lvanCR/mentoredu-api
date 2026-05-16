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
    private final String avatarUrl;
    private final String city;
    private final String bio;
    private final String profileType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProfileResponse(Profile profile) {
        this.id          = profile.getId();
        this.userId      = profile.getUserId();
        this.displayName = profile.getDisplayName();
        this.avatarUrl   = profile.getAvatarUrl();
        this.city        = profile.getCity();
        this.bio         = profile.getBio();
        this.profileType = profile.getProfileType();
        this.createdAt   = profile.getCreatedAt();
        this.updatedAt   = profile.getUpdatedAt();
    }
}

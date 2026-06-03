package com.mentoredu.profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mentoredu.profile.model.Profile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ProfileResponse {

    private final UUID    id;
    private final UUID    userId;
    private final String  displayName;
    private final String  avatarUrl;
    private final String  city;
    private final String  bio;
    private final String  profileType;
    private final long    followerCount;
    private final long    followingCount;
    @JsonProperty("isFollowing")
    private final boolean isFollowing;
    /** true solo si existe el registro en student_profiles para este usuario */
    private final boolean hasStudentProfile;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /** PATCH /profiles/me — sin contexto social ni datos de perfil especializado */
    public ProfileResponse(Profile profile) {
        this(profile, 0L, 0L, false, false);
    }

    public ProfileResponse(Profile profile, long followerCount, long followingCount,
                           boolean isFollowing, boolean hasStudentProfile) {
        this.id                = profile.getId();
        this.userId            = profile.getUserId();
        this.displayName       = profile.getDisplayName();
        this.avatarUrl         = profile.getAvatarUrl();
        this.city              = profile.getCity();
        this.bio               = profile.getBio();
        this.profileType       = profile.getProfileType();
        this.followerCount     = followerCount;
        this.followingCount    = followingCount;
        this.isFollowing       = isFollowing;
        this.hasStudentProfile = hasStudentProfile;
        this.createdAt         = profile.getCreatedAt();
        this.updatedAt         = profile.getUpdatedAt();
    }
}

package com.mentoredu.profile.dto;

import com.mentoredu.auth.entity.User;
import com.mentoredu.profile.model.Profile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ProfileMeResponse {

    private final UUID id;
    private final UUID userId;
    private final String displayName;
    private final String avatarUrl;
    private final String city;
    private final String bio;
    private final String profileType;
    private final boolean isProfileComplete;
    private final LocalDateTime createdAt;

    public ProfileMeResponse(Profile profile, boolean isProfileComplete) {
        this.id                = profile.getId();
        this.userId            = profile.getUserId();
        this.displayName       = profile.getDisplayName();
        this.avatarUrl         = profile.getAvatarUrl();
        this.city              = profile.getCity();
        this.bio               = profile.getBio();
        this.profileType       = profile.getProfileType();
        this.isProfileComplete = isProfileComplete;
        this.createdAt         = profile.getCreatedAt();
    }

    private ProfileMeResponse(UUID id, UUID userId, String displayName, String profileType,
                              LocalDateTime createdAt) {
        this.id                = id;
        this.userId            = userId;
        this.displayName       = displayName;
        this.avatarUrl         = null;
        this.city              = null;
        this.bio               = null;
        this.profileType       = profileType;
        this.isProfileComplete = true;
        this.createdAt         = createdAt;
    }

    /** Synthetic response for system users (ADMIN, MODERATOR) who have no profile record. */
    public static ProfileMeResponse forSystemUser(User user) {
        String displayName = (user.getFirstName() + " " + user.getLastName()).trim();
        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        return new ProfileMeResponse(null, user.getId(), displayName, roleName, user.getCreatedAt());
    }
}

package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.AcademyProfile;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AcademyProfileResponse(
    UUID profileId,
    String academyName,
    String ruc,
    String website,
    String contactEmail
) {
    public static AcademyProfileResponse from(AcademyProfile ap) {
        return AcademyProfileResponse.builder()
            .profileId(ap.getProfileId())
            .academyName(ap.getAcademyName())
            .ruc(ap.getRuc())
            .website(ap.getWebsite())
            .contactEmail(ap.getContactEmail())
            .build();
    }
}

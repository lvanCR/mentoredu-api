package com.mentoredu.profile.dto;

import com.mentoredu.profile.model.OrganizationProfile;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrganizationProfileResponse {

    private final UUID profileId;
    private final String organizationName;
    private final String ruc;
    private final String website;
    private final String contactEmail;

    public OrganizationProfileResponse(OrganizationProfile profile) {
        this.profileId        = profile.getProfileId();
        this.organizationName = profile.getOrganizationName();
        this.ruc              = profile.getRuc();
        this.website          = profile.getWebsite();
        this.contactEmail     = profile.getContactEmail();
    }
}

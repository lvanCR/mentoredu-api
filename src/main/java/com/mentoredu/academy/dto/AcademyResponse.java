package com.mentoredu.academy.dto;

import com.mentoredu.academy.model.Academy;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AcademyResponse {

    private final UUID          id;
    private final UUID          ownerProfileId;
    private final String        name;
    private final String        description;
    private final String        website;
    private final String        email;
    private final Boolean       active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AcademyResponse(Academy academy) {
        this.id             = academy.getId();
        this.ownerProfileId = academy.getOwnerProfileId();
        this.name           = academy.getName();
        this.description    = academy.getDescription();
        this.website        = academy.getWebsite();
        this.email          = academy.getEmail();
        this.active         = academy.getActive();
        this.createdAt      = academy.getCreatedAt();
        this.updatedAt      = academy.getUpdatedAt();
    }
}

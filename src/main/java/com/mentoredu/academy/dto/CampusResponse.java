package com.mentoredu.academy.dto;

import com.mentoredu.academy.model.Campus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CampusResponse {

    private final UUID          id;
    private final UUID          academyId;
    private final String        name;
    private final String        address;
    private final String        city;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CampusResponse(Campus campus) {
        this.id        = campus.getId();
        this.academyId = campus.getAcademyId();
        this.name      = campus.getName();
        this.address   = campus.getAddress();
        this.city      = campus.getCity();
        this.createdAt = campus.getCreatedAt();
        this.updatedAt = campus.getUpdatedAt();
    }
}

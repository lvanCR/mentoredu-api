package com.mentoredu.catalog.dto;

import com.mentoredu.catalog.model.Career;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CareerResponse(UUID id, UUID universityId, UUID areaId, String name, String description) {
    public static CareerResponse from(Career c) {
        return CareerResponse.builder()
            .id(c.getId())
            .universityId(c.getUniversity().getId())
            .areaId(c.getArea().getId())
            .name(c.getName())
            .description(c.getDescription())
            .build();
    }
}

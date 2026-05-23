package com.mentoredu.catalog.dto;

import com.mentoredu.catalog.model.Area;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AreaResponse(UUID id, UUID universityId, String name, String description) {
    public static AreaResponse from(Area a) {
        return AreaResponse.builder()
            .id(a.getId())
            .universityId(a.getUniversity().getId())
            .name(a.getName())
            .description(a.getDescription())
            .build();
    }
}

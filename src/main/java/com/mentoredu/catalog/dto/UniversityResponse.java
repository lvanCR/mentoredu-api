package com.mentoredu.catalog.dto;

import com.mentoredu.catalog.model.University;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UniversityResponse(UUID id, String name, String city) {
    public static UniversityResponse from(University u) {
        return UniversityResponse.builder()
            .id(u.getId())
            .name(u.getName())
            .city(u.getCity())
            .build();
    }
}

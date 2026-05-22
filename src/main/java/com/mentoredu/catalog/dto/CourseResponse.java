package com.mentoredu.catalog.dto;

import com.mentoredu.catalog.model.Course;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CourseResponse(UUID id, String name, String description) {
    public static CourseResponse from(Course c) {
        return CourseResponse.builder()
            .id(c.getId())
            .name(c.getName())
            .description(c.getDescription())
            .build();
    }
}

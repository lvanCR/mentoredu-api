package com.mentoredu.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ThreadResponse {
    private UUID          id;
    private String        title;
    private String        body;
    private boolean       anonymous;
    private String        authorDisplay;
    private String        status;
    private UUID          universityId;
    private UUID          areaId;
    private UUID          courseId;
    private UUID          careerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

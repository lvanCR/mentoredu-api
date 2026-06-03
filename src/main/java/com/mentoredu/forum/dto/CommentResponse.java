package com.mentoredu.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private UUID answerId;
    private UUID threadId;
    private String body;
    private UUID   authorId;
    private String authorDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

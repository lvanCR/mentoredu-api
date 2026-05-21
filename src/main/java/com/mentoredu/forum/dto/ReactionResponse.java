package com.mentoredu.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReactionResponse {
    private UUID id;
    private String targetType;
    private UUID targetId;
    private String reactionType;
    private UUID userId;
    private LocalDateTime createdAt;
}

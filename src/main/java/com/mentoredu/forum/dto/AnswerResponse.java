package com.mentoredu.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AnswerResponse {
    private UUID id;
    private UUID threadId;
    private String body;
    private boolean accepted;
    private UUID   authorId;
    private String authorDisplay;
    private int    likeCount;
    private int    dislikeCount;
    private String myReaction;   // "LIKE" | "DISLIKE" | null
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

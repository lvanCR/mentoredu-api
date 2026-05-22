package com.mentoredu.pedagogy.dto;

import com.mentoredu.pedagogy.model.FeedbackEntry;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FeedbackResponse(
    UUID id,
    UUID solutionId,
    UUID authorId,
    BigDecimal score,
    String body,
    LocalDateTime createdAt
) {
    public static FeedbackResponse from(FeedbackEntry f) {
        return FeedbackResponse.builder()
            .id(f.getId())
            .solutionId(f.getSolution().getId())
            .authorId(f.getAuthor().getId())
            .score(f.getScore())
            .body(f.getBody())
            .createdAt(f.getCreatedAt())
            .build();
    }
}

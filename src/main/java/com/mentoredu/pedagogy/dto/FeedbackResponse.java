package com.mentoredu.pedagogy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    UUID authorUserId,
    String authorName,
    String authorRole,
    BigDecimal score,
    String body,
    LocalDateTime createdAt
) {
    /** Versión base — usa firstName+lastName del User. */
    public static FeedbackResponse from(FeedbackEntry f) {
        String name = f.getAuthor().getFirstName() + " " + f.getAuthor().getLastName();
        String role = f.getAuthor().getRole() != null ? f.getAuthor().getRole().getName() : null;
        return FeedbackResponse.builder()
            .id(f.getId())
            .solutionId(f.getSolution().getId())
            .authorId(f.getAuthor().getId())
            .authorUserId(f.getAuthor().getId())
            .authorName(name)
            .authorRole(role)
            .score(f.getScore())
            .body(f.getBody())
            .createdAt(f.getCreatedAt())
            .build();
    }

    /** Versión enriquecida con el displayName del perfil. */
    public static FeedbackResponse from(FeedbackEntry f, String displayName) {
        String role = f.getAuthor().getRole() != null ? f.getAuthor().getRole().getName() : null;
        return FeedbackResponse.builder()
            .id(f.getId())
            .solutionId(f.getSolution().getId())
            .authorId(f.getAuthor().getId())
            .authorUserId(f.getAuthor().getId())
            .authorName(displayName)
            .authorRole(role)
            .score(f.getScore())
            .body(f.getBody())
            .createdAt(f.getCreatedAt())
            .build();
    }
}

package com.mentoredu.pedagogy.dto;

import com.mentoredu.pedagogy.model.FeedbackEntry;
import com.mentoredu.pedagogy.model.Solution;
import com.mentoredu.library.model.Resource;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MySolutionSummaryResponse(
    UUID          id,
    UUID          resourceId,
    String        resourceTitle,
    String        resourceType,
    String        status,
    LocalDateTime submittedAt,
    BigDecimal    feedbackScore,
    String        feedbackBody,
    LocalDateTime feedbackAt
) {
    public static MySolutionSummaryResponse of(Solution s, Resource r, FeedbackEntry f) {
        return MySolutionSummaryResponse.builder()
            .id(s.getId())
            .resourceId(r.getId())
            .resourceTitle(r.getTitle())
            .resourceType(r.getResourceType().name())
            .status(s.getStatus().name())
            .submittedAt(s.getSubmittedAt())
            .feedbackScore(f != null ? f.getScore() : null)
            .feedbackBody(f != null ? f.getBody() : null)
            .feedbackAt(f != null ? f.getCreatedAt() : null)
            .build();
    }
}

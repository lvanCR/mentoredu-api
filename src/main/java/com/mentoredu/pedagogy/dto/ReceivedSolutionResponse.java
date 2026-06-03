package com.mentoredu.pedagogy.dto;

import com.mentoredu.pedagogy.model.FeedbackEntry;
import com.mentoredu.pedagogy.model.Solution;
import com.mentoredu.library.model.Resource;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReceivedSolutionResponse(
    UUID          id,
    UUID          resourceId,
    String        resourceTitle,
    String        resourceType,
    UUID          studentId,
    String        studentName,
    String        status,
    LocalDateTime submittedAt,
    boolean       hasFeedback,
    BigDecimal    feedbackScore
) {
    public static ReceivedSolutionResponse of(Solution s, Resource r, FeedbackEntry f) {
        return ReceivedSolutionResponse.builder()
            .id(s.getId())
            .resourceId(r.getId())
            .resourceTitle(r.getTitle())
            .resourceType(r.getResourceType().name())
            .studentId(s.getStudent().getId())
            .studentName(s.getStudent().getFirstName() + " " + s.getStudent().getLastName())
            .status(s.getStatus().name())
            .submittedAt(s.getSubmittedAt())
            .hasFeedback(f != null)
            .feedbackScore(f != null ? f.getScore() : null)
            .build();
    }
}

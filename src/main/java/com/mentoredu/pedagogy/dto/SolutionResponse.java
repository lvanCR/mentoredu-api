package com.mentoredu.pedagogy.dto;

import com.mentoredu.pedagogy.model.Solution;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SolutionResponse(
    UUID id,
    UUID resourceId,
    UUID studentId,
    String studentName,
    String fileUrl,
    String content,
    String status,
    LocalDateTime submittedAt
) {
    public static SolutionResponse from(Solution s) {
        return SolutionResponse.builder()
            .id(s.getId())
            .resourceId(s.getResourceId())
            .studentId(s.getStudent().getId())
            .studentName(s.getStudent().getFirstName() + " " + s.getStudent().getLastName())
            .fileUrl(s.getFileUrl())
            .content(s.getContent())
            .status(s.getStatus().name())
            .submittedAt(s.getSubmittedAt())
            .build();
    }
}
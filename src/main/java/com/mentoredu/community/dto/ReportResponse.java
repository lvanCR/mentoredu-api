package com.mentoredu.community.dto;

import com.mentoredu.community.model.Report;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReportResponse(
    UUID id, UUID reporterId, String targetType, UUID targetId,
    String reason, String status, LocalDateTime createdAt
) {
    public static ReportResponse from(Report r) {
        return ReportResponse.builder()
            .id(r.getId())
            .reporterId(r.getReporter().getId())
            .targetType(r.getTargetType())
            .targetId(r.getTargetId())
            .reason(r.getReason())
            .status(r.getStatus())
            .createdAt(r.getCreatedAt())
            .build();
    }
}

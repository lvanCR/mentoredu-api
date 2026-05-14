package com.mentoredu.moderation.dto;

import com.mentoredu.moderation.model.Report;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ReportResponse {
    private final UUID id;
    private final String targetType;
    private final UUID targetId;
    private final String reason;
    private final String status;
    private final LocalDateTime createdAt;

    public ReportResponse(Report report) {
        this.id = report.getId();
        this.targetType = report.getTargetType().name();
        this.targetId = report.getTargetId();
        this.reason = report.getReason();
        this.status = report.getStatus().name();
        this.createdAt = report.getCreatedAt();
    }
}

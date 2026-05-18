package com.mentoredu.moderation.dto;

import com.mentoredu.moderation.model.ModerationAction;
import com.mentoredu.moderation.model.Report;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ResolveReportResponse {
    private final UUID id;
    private final String targetType;
    private final UUID targetId;
    private final String reason;
    private final String status;
    private final LocalDateTime createdAt;
    private final UUID resolvedBy;
    private final LocalDateTime resolvedAt;
    private final String actionType;
    private final String notes;

    public ResolveReportResponse(Report report, ModerationAction action) {
        this.id = report.getId();
        this.targetType = report.getTargetType().name();
        this.targetId = report.getTargetId();
        this.reason = report.getReason();
        this.status = report.getStatus().name();
        this.createdAt = report.getCreatedAt();
        this.resolvedBy = report.getResolvedBy();
        this.resolvedAt = report.getResolvedAt();
        this.actionType = action.getActionType();
        this.notes = action.getNotes();
    }
}

package com.mentoredu.moderation.dto;

import com.mentoredu.moderation.model.Appeal;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AppealResponse {
    private final UUID id;
    private final UUID reportId;
    private final String reason;
    private final String status;
    private final LocalDateTime createdAt;

    public AppealResponse(Appeal appeal) {
        this.id = appeal.getId();
        this.reportId = appeal.getReport().getId();
        this.reason = appeal.getReason();
        this.status = appeal.getStatus();
        this.createdAt = appeal.getCreatedAt();
    }
}

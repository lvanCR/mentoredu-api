package com.mentoredu.feedback.dto;

import com.mentoredu.feedback.model.FeedbackEntry;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class FeedbackResponse {
    private final UUID id;
    private final UUID authorUserId;
    private final UUID targetUserId;
    private final UUID programId;
    private final UUID cycleId;
    private final String body;
    private final BigDecimal score;
    private final LocalDateTime createdAt;

    public FeedbackResponse(FeedbackEntry entry) {
        this.id = entry.getId();
        this.authorUserId = entry.getAuthor().getId();
        this.targetUserId = entry.getTarget().getId();
        this.programId = entry.getProgramId();
        this.cycleId = entry.getCycleId();
        this.body = entry.getBody();
        this.score = entry.getScore();
        this.createdAt = entry.getCreatedAt();
    }
}

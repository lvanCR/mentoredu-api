package com.mentoredu.verification.dto;

import com.mentoredu.verification.model.VerificationRequest;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class VerificationRequestResponse {

    private final UUID id;
    private final UUID userId;
    private final String entityType;
    private final String status;
    private final LocalDateTime submittedAt;
    private final LocalDateTime reviewedAt;

    public VerificationRequestResponse(VerificationRequest request) {
        this.id = request.getId();
        this.userId = request.getUser().getId();
        this.entityType = request.getEntityType();
        this.status = request.getStatus();
        this.submittedAt = request.getSubmittedAt();
        this.reviewedAt = request.getReviewedAt();
    }
}

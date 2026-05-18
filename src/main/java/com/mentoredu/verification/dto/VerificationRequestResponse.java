package com.mentoredu.verification.dto;

import com.mentoredu.verification.model.VerificationDocument;
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
    private final String documentType;
    private final String fileUrl;

    public VerificationRequestResponse(VerificationRequest request, VerificationDocument document) {
        this.id = request.getId();
        this.userId = request.getUser().getId();
        this.entityType = request.getEntityType().name();
        this.status = request.getStatus().name();
        this.submittedAt = request.getSubmittedAt();
        this.reviewedAt = request.getReviewedAt();
        this.documentType = document != null ? document.getDocumentType() : null;
        this.fileUrl = document != null ? document.getFileUrl() : null;
    }
}

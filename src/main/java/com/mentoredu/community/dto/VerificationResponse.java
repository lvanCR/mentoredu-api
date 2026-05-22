package com.mentoredu.community.dto;

import com.mentoredu.community.model.VerificationDoc;
import com.mentoredu.community.model.VerificationRequest;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class VerificationResponse {

    private final UUID id;
    private final UUID userId;
    private final String entityType;
    private final String status;
    private final String notes;
    private final LocalDateTime submittedAt;
    private final LocalDateTime reviewedAt;
    private final List<DocInfo> documents;

    public VerificationResponse(VerificationRequest r) {
        this.id          = r.getId();
        this.userId      = r.getUser().getId();
        this.entityType  = r.getEntityType();
        this.status      = r.getStatus();
        this.notes       = r.getNotes();
        this.submittedAt = r.getSubmittedAt();
        this.reviewedAt  = r.getReviewedAt();
        this.documents   = r.getDocs() == null ? List.of()
                : r.getDocs().stream().map(DocInfo::new).toList();
    }

    @Getter
    public static class DocInfo {
        private final UUID id;
        private final String documentType;
        private final String fileUrl;
        private final LocalDateTime uploadedAt;

        public DocInfo(VerificationDoc d) {
            this.id           = d.getId();
            this.documentType = d.getDocumentType();
            this.fileUrl      = d.getFileUrl();
            this.uploadedAt   = d.getUploadedAt();
        }
    }
}

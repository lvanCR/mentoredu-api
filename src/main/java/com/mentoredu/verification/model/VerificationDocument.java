package com.mentoredu.verification.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class VerificationDocument {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private VerificationRequest request;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "file_url", columnDefinition = "text")
    private String fileUrl;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
    }
}

package com.mentoredu.document.model;

import com.mentoredu.auth.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DownloadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private LocalDateTime downloadedAt;

    @PrePersist
    protected void onCreate() {
        if (downloadedAt == null) downloadedAt = LocalDateTime.now();
    }
}

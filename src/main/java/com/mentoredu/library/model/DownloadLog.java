package com.mentoredu.library.model;

import com.mentoredu.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "download_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DownloadLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private AcademicResource resource;

    @Column(name = "downloaded_at", nullable = false)
    private LocalDateTime downloadedAt;

    @PrePersist
    protected void onCreate() {
        if (downloadedAt == null) downloadedAt = LocalDateTime.now();
    }
}

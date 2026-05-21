package com.mentoredu.feedback.model;

import com.mentoredu.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback_entries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FeedbackEntry {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User target;

    @Column(name = "program_id", columnDefinition = "uuid")
    private UUID programId;

    @Column(name = "cycle_id", columnDefinition = "uuid")
    private UUID cycleId;

    @Column(name = "solution_id", columnDefinition = "uuid")
    private UUID solutionId;

    @Column(name = "resource_id", columnDefinition = "uuid")
    private UUID resourceId;

    @Column(columnDefinition = "text", nullable = false)
    private String body;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }
}

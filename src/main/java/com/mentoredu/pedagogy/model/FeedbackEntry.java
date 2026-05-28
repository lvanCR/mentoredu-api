package com.mentoredu.pedagogy.model;

import com.mentoredu.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false, unique = true)
    private Solution solution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

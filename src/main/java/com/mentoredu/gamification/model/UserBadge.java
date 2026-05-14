package com.mentoredu.gamification.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_badges")
@IdClass(UserBadge.UserBadgeId.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserBadge {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Id
    @Column(name = "badge_id", columnDefinition = "uuid")
    private UUID badgeId;

    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    @PrePersist
    protected void onCreate() {
        if (earnedAt == null) earnedAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBadgeId implements Serializable {
        private UUID userId;
        private UUID badgeId;
    }
}

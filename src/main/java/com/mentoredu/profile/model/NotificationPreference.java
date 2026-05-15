package com.mentoredu.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled")
    private Boolean inAppEnabled = true;

    @Column(name = "forum_enabled")
    private Boolean forumEnabled = true;

    @Column(name = "moderation_enabled")
    private Boolean moderationEnabled = true;
}

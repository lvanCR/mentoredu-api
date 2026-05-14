package com.mentoredu.notification.dto;

import com.mentoredu.notification.model.Notification;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NotificationResponse {

    private final UUID id;
    private final String type;
    private final String title;
    private final String message;
    private final boolean read;
    private final LocalDateTime readAt;
    private final LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.read = notification.getReadAt() != null;
        this.readAt = notification.getReadAt();
        this.createdAt = notification.getCreatedAt();
    }
}

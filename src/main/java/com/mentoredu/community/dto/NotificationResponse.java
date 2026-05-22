package com.mentoredu.community.dto;

import com.mentoredu.community.model.Notification;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record NotificationResponse(
    UUID id, String type, Map<String, Object> payload,
    LocalDateTime readAt, LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .type(n.getType())
            .payload(n.getPayload())
            .readAt(n.getReadAt())
            .createdAt(n.getCreatedAt())
            .build();
    }
}

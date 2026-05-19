package com.mentoredu.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BadgeResponse {
    private UUID badgeId;
    private String code;
    private String name;
    private String description;
    private LocalDateTime earnedAt;
}

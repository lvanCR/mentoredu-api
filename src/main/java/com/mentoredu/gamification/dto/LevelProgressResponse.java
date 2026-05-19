package com.mentoredu.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LevelProgressResponse {
    private UUID userId;
    private Integer currentLevel;
    private Integer experience;
    private BigDecimal progressPercentage;
    private LocalDateTime updatedAt;
}

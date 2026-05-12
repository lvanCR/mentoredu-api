package com.mentoredu.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PointsResponse {
    private Long userId;
    private Integer points;
}
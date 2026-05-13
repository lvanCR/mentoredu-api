package com.mentoredu.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PointsResponse {
    private java.util.UUID userId;
    private Integer points;
}
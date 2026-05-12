package com.mentoredu.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoinsResponse {
    private Long userId;
    private Integer coins;
}
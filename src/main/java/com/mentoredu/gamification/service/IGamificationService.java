package com.mentoredu.gamification.service;

import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.PointsRequest;
import com.mentoredu.gamification.dto.PointsResponse;

import java.util.UUID;

public interface IGamificationService {
    PointsResponse getPoints(UUID userId);
    PointsResponse addPoints(UUID userId, PointsRequest request);
    CoinsResponse getCoins(UUID userId);
    CoinsResponse addCoins(UUID userId, CoinsRequest request);
    CoinsResponse redeemCoins(UUID userId, CoinsRequest request);
}

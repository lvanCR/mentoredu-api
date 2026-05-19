package com.mentoredu.gamification.service;

import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.LevelProgressResponse;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.model.enums.PointSourceType;

import java.util.UUID;

public interface IGamificationService {
    PointsResponse getPoints(UUID userId);
    void awardPoints(UUID userId, PointSourceType sourceType, UUID sourceId, int pointsDelta);
    CoinsResponse getCoins(UUID userId);
    CoinsResponse redeemCoins(UUID userId, CoinsRequest request);
    LevelProgressResponse getLevelProgress(UUID userId);
}

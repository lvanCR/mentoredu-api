package com.mentoredu.gamification.service;

import com.mentoredu.gamification.dto.BadgeResponse;
import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.LevelProgressResponse;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.model.enums.PointSourceType;

import java.util.List;
import java.util.UUID;

public interface IGamificationService {
    PointsResponse getPoints(UUID userId);
    void awardPoints(UUID userId, PointSourceType sourceType, UUID sourceId, int pointsDelta);
    void awardBadge(UUID userId, String badgeCode);
    CoinsResponse getCoins(UUID userId);
    CoinsResponse redeemCoins(UUID userId, CoinsRequest request);
    LevelProgressResponse getLevelProgress(UUID userId);
    List<BadgeResponse> getBadges(UUID userId);
}

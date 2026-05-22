package com.mentoredu.gamification.event;

import java.util.UUID;

public record BadgeAwardedEvent(
        UUID userId,
        String badgeCode,
        String badgeName
) {}

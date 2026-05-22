package com.mentoredu.gamification.event;

import java.util.UUID;

public record LevelUpEvent(
        UUID userId,
        int newLevel
) {}

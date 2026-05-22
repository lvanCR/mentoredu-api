package com.mentoredu.moderation.event;

import java.util.UUID;

public record AppealResolvedEvent(
        UUID userId,
        UUID appealId,
        String newStatus
) {}

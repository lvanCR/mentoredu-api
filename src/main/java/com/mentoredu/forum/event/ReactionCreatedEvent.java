package com.mentoredu.forum.event;

import java.util.UUID;

public record ReactionCreatedEvent(
        UUID reactorId,
        UUID contentAuthorId,
        String targetType,
        UUID targetId,
        String reactionType
) {}

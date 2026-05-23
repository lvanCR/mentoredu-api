package com.mentoredu.community.event;

import java.util.UUID;

public record VerificationProcessedEvent(
        UUID requestId,
        UUID requesterId,
        String entityType,
        String status,
        String notes
) {}

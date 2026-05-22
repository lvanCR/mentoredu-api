package com.mentoredu.verification.event;

import java.util.UUID;

public record VerificationProcessedEvent(
        UUID userId,
        String entityType,
        String newStatus
) {}

package com.mentoredu.community.event;

import com.mentoredu.community.model.VerificationStatus;

import java.util.UUID;

public record VerificationProcessedEvent(
        UUID requestId,
        UUID requesterId,
        String entityType,
        VerificationStatus status,
        String notes
) {}

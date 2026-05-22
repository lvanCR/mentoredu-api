package com.mentoredu.feedback.event;

import java.util.UUID;

public record SolutionReviewedEvent(
        UUID solutionId,
        UUID studentId,
        UUID resourceId,
        String resourceTitle
) {}

package com.mentoredu.pedagogy.event;

import java.util.UUID;

public record FeedbackGivenEvent(
        UUID feedbackId,
        UUID solutionId,
        UUID studentId,
        UUID resourceId
) {}

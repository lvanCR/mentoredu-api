package com.mentoredu.feedback.event;

import java.util.UUID;

public record FeedbackGivenEvent(
        UUID feedbackId,
        UUID targetUserId,
        UUID authorId,
        String authorName
) {}

package com.mentoredu.forum.event;

import java.util.UUID;

public record AnswerCreatedEvent(
        UUID answerId,
        UUID threadAuthorId,
        UUID answerAuthorId,
        String threadTitle
) {}

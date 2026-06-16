package com.mentoredu.forum.event;

import java.util.UUID;

public record CommentCreatedEvent(
        UUID commentId,
        UUID threadId,
        UUID answerAuthorId,
        UUID commentAuthorId,
        String truncatedBody
) {}
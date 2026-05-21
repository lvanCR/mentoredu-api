package com.mentoredu.moderation.exception;

public class UnauthorizedModerationException extends RuntimeException {
    public UnauthorizedModerationException(String message) {
        super(message);
    }
}

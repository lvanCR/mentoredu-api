package com.mentoredu.moderation.exception;

public class AppealAlreadyResolvedException extends RuntimeException {
    public AppealAlreadyResolvedException(String message) {
        super(message);
    }
}

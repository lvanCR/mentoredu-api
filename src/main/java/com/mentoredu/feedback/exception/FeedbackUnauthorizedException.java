package com.mentoredu.feedback.exception;

public class FeedbackUnauthorizedException extends RuntimeException {
    public FeedbackUnauthorizedException(String message) {
        super(message);
    }
}

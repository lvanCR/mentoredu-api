package com.mentoredu.moderation.exception;

public class ReportedContentNotFoundException extends RuntimeException {
    public ReportedContentNotFoundException(String message) {
        super(message);
    }
}

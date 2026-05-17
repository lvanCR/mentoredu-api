package com.mentoredu.moderation.exception;

public class DuplicateReportException extends RuntimeException {
    public DuplicateReportException(String message) {
        super(message);
    }
}

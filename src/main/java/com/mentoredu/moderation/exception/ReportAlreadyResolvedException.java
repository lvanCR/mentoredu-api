package com.mentoredu.moderation.exception;

public class ReportAlreadyResolvedException extends RuntimeException {
    public ReportAlreadyResolvedException(String message) {
        super(message);
    }
}

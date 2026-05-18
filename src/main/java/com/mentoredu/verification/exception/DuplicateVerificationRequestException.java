package com.mentoredu.verification.exception;

public class DuplicateVerificationRequestException extends RuntimeException {
    public DuplicateVerificationRequestException(String message) {
        super(message);
    }
}

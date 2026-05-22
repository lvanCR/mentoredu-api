package com.mentoredu.verification.exception;

public class VerificationAlreadyProcessedException extends RuntimeException {
    public VerificationAlreadyProcessedException(String message) {
        super(message);
    }
}

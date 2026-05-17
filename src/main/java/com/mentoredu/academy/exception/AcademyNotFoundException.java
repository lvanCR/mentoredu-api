package com.mentoredu.academy.exception;

public class AcademyNotFoundException extends RuntimeException {
    public AcademyNotFoundException(String message) {
        super(message);
    }
}

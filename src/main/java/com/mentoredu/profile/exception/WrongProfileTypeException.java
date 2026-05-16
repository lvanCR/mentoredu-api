package com.mentoredu.profile.exception;

public class WrongProfileTypeException extends RuntimeException {
    public WrongProfileTypeException(String message) {
        super(message);
    }
}

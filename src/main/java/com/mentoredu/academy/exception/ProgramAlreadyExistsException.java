package com.mentoredu.academy.exception;

public class ProgramAlreadyExistsException extends RuntimeException {
    public ProgramAlreadyExistsException(String message) {
        super(message);
    }
}

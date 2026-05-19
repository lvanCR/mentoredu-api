package com.mentoredu.academy.exception;

public class CampusAlreadyExistsException extends RuntimeException {
    public CampusAlreadyExistsException(String message) {
        super(message);
    }
}

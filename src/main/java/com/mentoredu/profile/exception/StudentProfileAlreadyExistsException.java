package com.mentoredu.profile.exception;

public class StudentProfileAlreadyExistsException extends RuntimeException {
    public StudentProfileAlreadyExistsException(String message) {
        super(message);
    }
}

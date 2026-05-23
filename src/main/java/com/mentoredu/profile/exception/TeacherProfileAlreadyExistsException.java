package com.mentoredu.profile.exception;

public class TeacherProfileAlreadyExistsException extends RuntimeException {
    public TeacherProfileAlreadyExistsException(String message) {
        super(message);
    }
}

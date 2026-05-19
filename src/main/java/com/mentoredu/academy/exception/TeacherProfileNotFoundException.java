package com.mentoredu.academy.exception;

public class TeacherProfileNotFoundException extends RuntimeException {
    public TeacherProfileNotFoundException(String message) {
        super(message);
    }
}

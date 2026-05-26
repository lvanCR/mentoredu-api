package com.mentoredu.library.exception;

public class CourseIdRequiredException extends RuntimeException {
    public CourseIdRequiredException(String message) {
        super(message);
    }
}

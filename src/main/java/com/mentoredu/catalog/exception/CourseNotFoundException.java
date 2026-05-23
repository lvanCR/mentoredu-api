package com.mentoredu.catalog.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String message) { super(message); }
}

package com.mentoredu.forum.exception;

public class ThreadNotFoundException extends RuntimeException {
    public ThreadNotFoundException(String message) {
        super(message);
    }
}

package com.mentoredu.forum.exception;

public class ThreadClosedException extends RuntimeException {
    public ThreadClosedException(String message) {
        super(message);
    }
}

package com.mentoredu.forum.exception;

public class ThreadNotOwnedException extends RuntimeException {
    public ThreadNotOwnedException(String message) {
        super(message);
    }
}

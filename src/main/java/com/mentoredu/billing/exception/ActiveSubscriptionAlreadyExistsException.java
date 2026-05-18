package com.mentoredu.billing.exception;

public class ActiveSubscriptionAlreadyExistsException extends RuntimeException {
    public ActiveSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}

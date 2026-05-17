package com.mentoredu.profile.exception;

public class OrganizationNameAlreadyExistsException extends RuntimeException {
    public OrganizationNameAlreadyExistsException(String message) {
        super(message);
    }
}

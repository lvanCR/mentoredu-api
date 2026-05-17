package com.mentoredu.profile.exception;

public class OrganizationProfileAlreadyExistsException extends RuntimeException {
    public OrganizationProfileAlreadyExistsException(String message) {
        super(message);
    }
}

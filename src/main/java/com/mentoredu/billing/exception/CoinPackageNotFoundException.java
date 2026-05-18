package com.mentoredu.billing.exception;

public class CoinPackageNotFoundException extends RuntimeException {
    public CoinPackageNotFoundException(String message) {
        super(message);
    }
}

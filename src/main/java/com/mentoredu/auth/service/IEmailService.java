package com.mentoredu.auth.service;

public interface IEmailService {
    void sendPasswordResetEmail(String to, String token);
}

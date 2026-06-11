package com.mentoredu.auth.service;

public interface IEmailService {
    void sendPasswordResetEmail(String to, String token);
    void sendContactNotification(String name, String email, String phone, String category, String institution);
}

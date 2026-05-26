package com.mentoredu.auth.event;

public record PasswordResetRequestedEvent(String email, String rawToken) {}

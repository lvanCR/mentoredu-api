package com.mentoredu.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgotPasswordResponse {

    private String message;

    /**
     * Token returned only in non-production environments (no email service configured).
     * In production, this would be sent via email instead.
     */
    private String token;
}

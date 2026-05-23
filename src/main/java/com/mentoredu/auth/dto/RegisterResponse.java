package com.mentoredu.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RegisterResponse {
    private UUID   id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String status;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long   expiresIn;
}

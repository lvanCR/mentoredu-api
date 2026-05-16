package com.mentoredu.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long   expiresIn;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID   id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
    }
}

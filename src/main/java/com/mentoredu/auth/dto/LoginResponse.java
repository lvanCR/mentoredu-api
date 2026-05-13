package com.mentoredu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private java.util.UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}

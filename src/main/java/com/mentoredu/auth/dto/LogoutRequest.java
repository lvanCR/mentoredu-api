package com.mentoredu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {

    @NotBlank(message = "El token de sesión es obligatorio")
    private String refreshToken;
}

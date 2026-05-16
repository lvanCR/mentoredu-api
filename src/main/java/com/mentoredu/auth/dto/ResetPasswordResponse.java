package com.mentoredu.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordResponse {

    private String message;
}

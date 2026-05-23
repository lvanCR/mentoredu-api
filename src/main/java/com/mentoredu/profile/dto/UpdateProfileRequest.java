package com.mentoredu.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "displayName is required")
    @Size(max = 120, message = "displayName must not exceed 120 characters")
    private String displayName;

    private String avatarUrl;

    @Size(max = 80, message = "city must not exceed 80 characters")
    private String city;

    private String bio;
}

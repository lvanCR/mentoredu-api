package com.mentoredu.academy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAcademyRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must not exceed 120 characters")
    private String name;

    private String description;

    @Size(max = 255, message = "website must not exceed 255 characters")
    private String website;

    @Email(message = "email must be a valid email address")
    @Size(max = 120, message = "email must not exceed 120 characters")
    private String email;
}

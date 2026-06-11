package com.mentoredu.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 120) String email,
    @Size(max = 20) String phone,
    @Size(max = 50) String category,
    @Size(max = 200) String institution
) {}

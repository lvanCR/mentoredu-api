package com.mentoredu.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAcademyProfileRequest(
    @NotBlank @Size(max = 120) String academyName,
    @Size(max = 20) String ruc,
    @Size(max = 255) String website,
    @Size(max = 120) String contactEmail
) {}

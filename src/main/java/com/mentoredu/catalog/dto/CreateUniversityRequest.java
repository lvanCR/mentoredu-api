package com.mentoredu.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUniversityRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 80) String city
) {}

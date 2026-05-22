package com.mentoredu.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAreaRequest(
    @NotBlank @Size(max = 120) String name,
    String description
) {}

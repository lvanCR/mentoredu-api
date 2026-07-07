package com.mentoredu.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBodyRequest(
    @NotBlank @Size(min = 2, max = 5000) String body
) {}

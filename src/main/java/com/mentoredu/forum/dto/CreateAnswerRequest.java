package com.mentoredu.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAnswerRequest(
    @NotBlank(message = "Body is required") @Size(max = 5000) String body
) {}

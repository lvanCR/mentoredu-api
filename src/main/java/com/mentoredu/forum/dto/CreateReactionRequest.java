package com.mentoredu.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReactionRequest(
    @NotBlank(message = "reactionType is required") @Size(max = 20) String reactionType
) {}

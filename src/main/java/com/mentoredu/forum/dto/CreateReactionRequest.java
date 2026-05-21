package com.mentoredu.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReactionRequest {

    @NotBlank(message = "reactionType is required")
    @Size(max = 20, message = "reactionType must not exceed 20 characters")
    private String reactionType;
}

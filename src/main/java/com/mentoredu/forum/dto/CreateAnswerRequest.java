package com.mentoredu.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAnswerRequest {

    @NotBlank(message = "Body is required")
    private String body;
}

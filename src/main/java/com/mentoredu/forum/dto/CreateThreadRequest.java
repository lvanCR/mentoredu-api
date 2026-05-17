package com.mentoredu.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateThreadRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 160, message = "Title must not exceed 160 characters")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    private boolean anonymous = false;
}

package com.mentoredu.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateThreadRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String body;
    private boolean anonymous = false;
}

package com.mentoredu.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
public class SubmitSolutionRequest {

    @NotNull(message = "El fileId es obligatorio")
    private UUID fileId;
}

package com.mentoredu.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SubmissionStatusDto {
    private final UUID          id;
    private final String        status;
    private final LocalDateTime submittedAt;
}

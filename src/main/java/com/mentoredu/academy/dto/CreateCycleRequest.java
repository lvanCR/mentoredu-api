package com.mentoredu.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCycleRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must not exceed 120 characters")
    private String name;

    @NotBlank(message = "cycleType is required")
    @Size(max = 30, message = "cycleType must not exceed 30 characters")
    private String cycleType;

    @NotBlank(message = "startDate is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "startDate must be in format yyyy-MM-dd")
    private String startDate;

    @NotBlank(message = "endDate is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "endDate must be in format yyyy-MM-dd")
    private String endDate;
}

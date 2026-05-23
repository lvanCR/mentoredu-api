package com.mentoredu.pedagogy.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateFeedbackRequest(
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal score,
    @NotBlank String body
) {}

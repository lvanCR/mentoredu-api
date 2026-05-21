package com.mentoredu.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSubscriptionRequest {

    @NotNull(message = "planId es obligatorio")
    private UUID planId;

    @NotBlank(message = "paymentMethod es obligatorio")
    private String paymentMethod;
}

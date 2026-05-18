package com.mentoredu.billing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BuyCoinPackageRequest {

    @NotNull(message = "coinPackageId es obligatorio")
    private UUID coinPackageId;

    @NotNull(message = "quantity es obligatorio")
    @Min(value = 1, message = "quantity debe ser al menos 1")
    private Integer quantity;

    @NotBlank(message = "paymentMethod es obligatorio")
    private String paymentMethod;
}

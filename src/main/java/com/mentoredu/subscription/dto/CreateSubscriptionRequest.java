package com.mentoredu.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSubscriptionRequest {
    @NotNull private UUID planId;
}

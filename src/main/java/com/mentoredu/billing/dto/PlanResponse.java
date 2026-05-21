package com.mentoredu.billing.dto;

import com.mentoredu.billing.model.Plan;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PlanResponse {

    private final UUID id;
    private final String name;
    private final BigDecimal price;
    private final Integer durationDays;
    private final String description;

    public PlanResponse(Plan plan) {
        this.id = plan.getId();
        this.name = plan.getName();
        this.price = plan.getPrice();
        this.durationDays = plan.getDurationDays();
        this.description = plan.getDescription();
    }
}

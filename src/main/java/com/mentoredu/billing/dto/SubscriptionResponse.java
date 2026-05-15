package com.mentoredu.billing.dto;

import com.mentoredu.billing.model.Subscription;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SubscriptionResponse {

    private final UUID id;
    private final UUID userId;
    private final UUID planId;
    private final String planName;
    private final BigDecimal planPrice;
    private final String status;
    private final LocalDateTime startsAt;
    private final LocalDateTime endsAt;
    private final LocalDateTime createdAt;

    public SubscriptionResponse(Subscription subscription) {
        this.id = subscription.getId();
        this.userId = subscription.getUser().getId();
        this.planId = subscription.getPlan().getId();
        this.planName = subscription.getPlan().getName();
        this.planPrice = subscription.getPlan().getPrice();
        this.status = subscription.getStatus().name();
        this.startsAt = subscription.getStartsAt();
        this.endsAt = subscription.getEndsAt();
        this.createdAt = subscription.getCreatedAt();
    }
}

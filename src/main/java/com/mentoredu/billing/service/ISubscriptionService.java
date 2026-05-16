package com.mentoredu.billing.service;

import com.mentoredu.billing.dto.CreateSubscriptionRequest;
import com.mentoredu.billing.dto.SubscriptionResponse;

import java.util.List;
import java.util.Optional;

public interface ISubscriptionService {
    SubscriptionResponse create(String userEmail, CreateSubscriptionRequest request);
    List<SubscriptionResponse> getUserSubscriptions(String userEmail);
    Optional<SubscriptionResponse> getActiveSubscription(String userEmail);
}

package com.mentoredu.subscription.service;

import com.mentoredu.subscription.dto.CreateSubscriptionRequest;
import com.mentoredu.subscription.dto.SubscriptionResponse;

import java.util.List;
import java.util.Optional;

public interface ISubscriptionService {
    SubscriptionResponse create(String userEmail, CreateSubscriptionRequest request);
    List<SubscriptionResponse> getUserSubscriptions(String userEmail);
    Optional<SubscriptionResponse> getActiveSubscription(String userEmail);
}

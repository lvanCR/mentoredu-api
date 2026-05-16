package com.mentoredu.billing.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.billing.dto.CreateSubscriptionRequest;
import com.mentoredu.billing.dto.SubscriptionResponse;
import com.mentoredu.billing.model.Plan;
import com.mentoredu.billing.model.Subscription;
import com.mentoredu.billing.model.enums.SubscriptionStatus;
import com.mentoredu.billing.repository.PlanRepository;
import com.mentoredu.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SubscriptionResponse create(String userEmail, CreateSubscriptionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        LocalDateTime startsAt = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(startsAt)
                .endsAt(startsAt.plusDays(plan.getDurationDays()))
                .build();
        return new SubscriptionResponse(subscriptionRepository.save(subscription));
    }

    @Override
    public List<SubscriptionResponse> getUserSubscriptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return subscriptionRepository.findByUserId(user.getId()).stream()
                .map(SubscriptionResponse::new)
                .toList();
    }

    @Override
    public Optional<SubscriptionResponse> getActiveSubscription(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .map(SubscriptionResponse::new);
    }
}

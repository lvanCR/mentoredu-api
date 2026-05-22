package com.mentoredu.billing.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.billing.dto.CreateSubscriptionRequest;
import com.mentoredu.billing.dto.SubscriptionResponse;
import com.mentoredu.billing.exception.ActiveSubscriptionAlreadyExistsException;
import com.mentoredu.billing.exception.PlanNotFoundException;
import com.mentoredu.billing.model.Payment;
import com.mentoredu.billing.model.Plan;
import com.mentoredu.billing.model.PremiumAccess;
import com.mentoredu.billing.model.Subscription;
import com.mentoredu.billing.model.enums.PaymentStatus;
import com.mentoredu.billing.model.enums.SubscriptionStatus;
import com.mentoredu.billing.repository.PaymentRepository;
import com.mentoredu.billing.repository.PlanRepository;
import com.mentoredu.billing.repository.PremiumAccessRepository;
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
    private final PaymentRepository paymentRepository;
    private final PremiumAccessRepository premiumAccessRepository;

    @Override
    @Transactional
    public SubscriptionResponse create(String userEmail, CreateSubscriptionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new PlanNotFoundException("Usuario no encontrado: " + userEmail));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado: " + request.getPlanId()));

        // RN-26: un usuario solo puede tener una suscripción premium activa a la vez
        subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new ActiveSubscriptionAlreadyExistsException(
                            "Ya tienes una suscripción activa hasta: " + existing.getEndsAt());
                });

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusDays(plan.getDurationDays());

        Subscription subscription = subscriptionRepository.save(
                Subscription.builder()
                        .user(user)
                        .plan(plan)
                        .status(SubscriptionStatus.ACTIVE)
                        .startsAt(now)
                        .endsAt(endsAt)
                        .build());

        // Registro de pago (pago simulado como aprobado — RN-28)
        paymentRepository.save(
                Payment.builder()
                        .user(user)
                        .subscription(subscription)
                        .amount(plan.getPrice())
                        .currency("PEN")
                        .paymentMethod(request.getPaymentMethod())
                        .status(PaymentStatus.APPROVED)
                        .build());

        // Acceso premium ligado al período de la suscripción (RN-27)
        premiumAccessRepository.save(
                PremiumAccess.builder()
                        .user(user)
                        .grantedAt(now)
                        .expiresAt(endsAt)
                        .build());

        return new SubscriptionResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getUserSubscriptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new PlanNotFoundException("Usuario no encontrado: " + userEmail));
        return subscriptionRepository.findByUserId(user.getId()).stream()
                .map(SubscriptionResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> getActiveSubscription(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new PlanNotFoundException("Usuario no encontrado: " + userEmail));
        return subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .map(SubscriptionResponse::new);
    }
}

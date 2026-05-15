package com.mentoredu.billing.controller;

import com.mentoredu.billing.dto.CreateSubscriptionRequest;
import com.mentoredu.billing.dto.SubscriptionResponse;
import com.mentoredu.billing.service.ISubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Suscripciones", description = "Gestión de suscripciones premium y modelo freemium (US23).")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;

    @GetMapping("/me")
    @Operation(summary = "US23 - Listar mis suscripciones")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(userDetails.getUsername()));
    }

    @GetMapping("/me/active")
    @Operation(summary = "US23 - Consultar suscripción activa")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        return subscriptionService.getActiveSubscription(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "US23 - Crear suscripción")
    public ResponseEntity<SubscriptionResponse> create(
            @Valid @RequestBody CreateSubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.create(userDetails.getUsername(), request));
    }
}

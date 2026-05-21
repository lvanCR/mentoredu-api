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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Billing — Suscripciones", description = "Activación y consulta de suscripciones premium (US23).")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;

    // -------------------------------------------------------------------------
    // US23 — Activate premium subscription
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
        summary = "US23 - Activar suscripción premium",
        description = "Crea una suscripción premium para el usuario autenticado. "
            + "Solo puede haber una suscripción activa a la vez (RN-26). "
            + "El pago se simula como aprobado; una suscripción activa genera un registro de PremiumAccess (RN-27). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<SubscriptionResponse> activate(@Valid @RequestBody CreateSubscriptionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.create(auth.getName(), request));
    }

    @GetMapping("/me")
    @Operation(summary = "US23 - Listar mis suscripciones")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(auth.getName()));
    }

    @GetMapping("/me/active")
    @Operation(summary = "US23 - Consultar suscripción activa")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return subscriptionService.getActiveSubscription(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

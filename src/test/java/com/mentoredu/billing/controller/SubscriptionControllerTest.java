package com.mentoredu.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.billing.dto.CreateSubscriptionRequest;
import com.mentoredu.billing.dto.SubscriptionResponse;
import com.mentoredu.billing.exception.ActiveSubscriptionAlreadyExistsException;
import com.mentoredu.billing.exception.PaymentFailedException;
import com.mentoredu.billing.exception.PlanNotFoundException;
import com.mentoredu.billing.model.Plan;
import com.mentoredu.billing.model.Subscription;
import com.mentoredu.billing.model.enums.SubscriptionStatus;
import com.mentoredu.billing.service.ISubscriptionService;
import com.mentoredu.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISubscriptionService subscriptionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // US23 — POST /api/v1/billing/subscriptions
    // =========================================================================

    // Gherkin: Exitoso — pago completo → suscripción activada (201)
    @Test
    @WithMockUser(username = "user@example.com")
    void activate_validPayment_returns201() throws Exception {
        CreateSubscriptionRequest request = buildRequest(UUID.randomUUID(), "CARD");
        SubscriptionResponse response = buildResponse(SubscriptionStatus.ACTIVE, 30);

        when(subscriptionService.create(eq("user@example.com"), any(CreateSubscriptionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/billing/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.planName").value("MENSUAL"))
                .andExpect(jsonPath("$.startsAt").isNotEmpty())
                .andExpect(jsonPath("$.endsAt").isNotEmpty());
    }

    // Gherkin: Error — pago falla → sistema no habilita el plan (400)
    @Test
    @WithMockUser(username = "user@example.com")
    void activate_paymentFails_returns400() throws Exception {
        CreateSubscriptionRequest request = buildRequest(UUID.randomUUID(), "INVALID_METHOD");

        when(subscriptionService.create(eq("user@example.com"), any(CreateSubscriptionRequest.class)))
                .thenThrow(new PaymentFailedException("El pago fue rechazado"));

        mockMvc.perform(post("/api/v1/billing/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El pago fue rechazado"));
    }

    // Gherkin: Alternativo exitoso — plan premium válido → acceso premium activo (201)
    @Test
    @WithMockUser(username = "user@example.com")
    void activate_premiumPlan_premiumGranted_returns201() throws Exception {
        CreateSubscriptionRequest request = buildRequest(UUID.randomUUID(), "YAPE");
        SubscriptionResponse response = buildResponse(SubscriptionStatus.ACTIVE, 90);

        when(subscriptionService.create(eq("user@example.com"), any(CreateSubscriptionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/billing/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.planPrice").isNotEmpty());
    }

    // Gherkin: Alternativo error — ya tiene suscripción activa → 409 Conflict (RN-26)
    @Test
    @WithMockUser(username = "user@example.com")
    void activate_alreadyHasActiveSubscription_returns409() throws Exception {
        CreateSubscriptionRequest request = buildRequest(UUID.randomUUID(), "CARD");

        when(subscriptionService.create(eq("user@example.com"), any(CreateSubscriptionRequest.class)))
                .thenThrow(new ActiveSubscriptionAlreadyExistsException(
                        "Ya tienes una suscripción activa hasta: " + LocalDateTime.now().plusDays(15)));

        mockMvc.perform(post("/api/v1/billing/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ya tienes una suscripción activa")));
    }

    // Extra: plan no encontrado → 404 Not Found
    @Test
    @WithMockUser(username = "user@example.com")
    void activate_planNotFound_returns404() throws Exception {
        UUID planId = UUID.randomUUID();
        CreateSubscriptionRequest request = buildRequest(planId, "CARD");

        when(subscriptionService.create(eq("user@example.com"), any(CreateSubscriptionRequest.class)))
                .thenThrow(new PlanNotFoundException("Plan no encontrado: " + planId));

        mockMvc.perform(post("/api/v1/billing/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Plan no encontrado")));
    }

    // Extra: campos obligatorios ausentes → 400 Validation failed
    @Test
    @WithMockUser(username = "user@example.com")
    void activate_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/billing/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Extra: sin autenticación → 401 Unauthorized
    @Test
    void activate_withoutAuth_returns401() throws Exception {
        CreateSubscriptionRequest request = buildRequest(UUID.randomUUID(), "CARD");

        mockMvc.perform(post("/api/v1/billing/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // US23 — GET /api/v1/billing/subscriptions/me
    // =========================================================================

    // Extra: usuario autenticado lista sus suscripciones → 200 OK
    @Test
    @WithMockUser(username = "user@example.com")
    void getMySubscriptions_authenticated_returns200() throws Exception {
        SubscriptionResponse response = buildResponse(SubscriptionStatus.ACTIVE, 30);

        when(subscriptionService.getUserSubscriptions("user@example.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/billing/subscriptions/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].planName").value("MENSUAL"));
    }

    // =========================================================================
    // US23 — GET /api/v1/billing/subscriptions/me/active
    // =========================================================================

    // Extra: tiene suscripción activa → 200 OK con datos
    @Test
    @WithMockUser(username = "user@example.com")
    void getActiveSubscription_hasActive_returns200() throws Exception {
        SubscriptionResponse response = buildResponse(SubscriptionStatus.ACTIVE, 30);

        when(subscriptionService.getActiveSubscription("user@example.com"))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/billing/subscriptions/me/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.endsAt").isNotEmpty());
    }

    // Extra: no tiene suscripción activa → 404 Not Found
    @Test
    @WithMockUser(username = "user@example.com")
    void getActiveSubscription_noActive_returns404() throws Exception {
        when(subscriptionService.getActiveSubscription("user@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/billing/subscriptions/me/active"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreateSubscriptionRequest buildRequest(UUID planId, String paymentMethod) {
        CreateSubscriptionRequest req = new CreateSubscriptionRequest();
        req.setPlanId(planId);
        req.setPaymentMethod(paymentMethod);
        return req;
    }

    private SubscriptionResponse buildResponse(SubscriptionStatus status, int durationDays) {
        User user = new User();
        user.setId(UUID.randomUUID());

        Plan plan = Plan.builder()
                .id(UUID.randomUUID())
                .name("MENSUAL")
                .price(new BigDecimal("19.90"))
                .durationDays(durationDays)
                .description("Acceso premium mensual")
                .build();

        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(user)
                .plan(plan)
                .status(status)
                .startsAt(now)
                .endsAt(now.plusDays(durationDays))
                .createdAt(now)
                .build();

        return new SubscriptionResponse(subscription);
    }
}

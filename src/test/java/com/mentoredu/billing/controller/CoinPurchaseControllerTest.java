package com.mentoredu.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.util.JwtUtil;
import com.mentoredu.billing.dto.BuyCoinPackageRequest;
import com.mentoredu.billing.dto.CoinPackageResponse;
import com.mentoredu.billing.dto.CoinPurchaseResponse;
import com.mentoredu.billing.exception.CoinPackageNotFoundException;
import com.mentoredu.billing.exception.PaymentFailedException;
import com.mentoredu.billing.model.CoinPackage;
import com.mentoredu.billing.model.CoinPurchase;
import com.mentoredu.billing.model.enums.CoinPurchaseStatus;
import com.mentoredu.billing.service.ICoinPurchaseService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CoinPurchaseController.class)
class CoinPurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICoinPurchaseService coinPurchaseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // US24 — POST /api/v1/billing/coin-purchases
    // =========================================================================

    // Gherkin: Exitoso — pago confirmado → monedas acreditadas (201)
    @Test
    @WithMockUser(username = "user@example.com")
    void buy_paymentConfirmed_returns201() throws Exception {
        UUID packageId = UUID.randomUUID();
        BuyCoinPackageRequest request = buildRequest(packageId, 1, "CARD");
        CoinPurchaseResponse response = buildPurchaseResponse(packageId, 1, 100, 100);

        when(coinPurchaseService.buy(eq("user@example.com"), any(BuyCoinPackageRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/billing/coin-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalCoins").value(100))
                .andExpect(jsonPath("$.newWalletBalance").value(100));
    }

    // Gherkin: Error — pago falla → sistema no acredita saldo (400)
    @Test
    @WithMockUser(username = "user@example.com")
    void buy_paymentFails_returns400() throws Exception {
        BuyCoinPackageRequest request = buildRequest(UUID.randomUUID(), 1, "INVALID_METHOD");

        when(coinPurchaseService.buy(eq("user@example.com"), any(BuyCoinPackageRequest.class)))
                .thenThrow(new PaymentFailedException("El pago fue rechazado"));

        mockMvc.perform(post("/api/v1/billing/coin-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El pago fue rechazado"));
    }

    // Gherkin: Alternativo exitoso — paquete disponible → saldo incrementado (201)
    @Test
    @WithMockUser(username = "user@example.com")
    void buy_validPackage_coinsIncremented_returns201() throws Exception {
        UUID packageId = UUID.randomUUID();
        BuyCoinPackageRequest request = buildRequest(packageId, 2, "YAPE");
        // Comprar 2 paquetes de 300 monedas = 600 monedas; wallet tenía 100, ahora 700
        CoinPurchaseResponse response = buildPurchaseResponse(packageId, 2, 600, 700);

        when(coinPurchaseService.buy(eq("user@example.com"), any(BuyCoinPackageRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/billing/coin-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalCoins").value(600))
                .andExpect(jsonPath("$.newWalletBalance").value(700));
    }

    // Gherkin: Alternativo error — paquete no existe → 404 Not Found
    @Test
    @WithMockUser(username = "user@example.com")
    void buy_packageNotFound_returns404() throws Exception {
        UUID packageId = UUID.randomUUID();
        BuyCoinPackageRequest request = buildRequest(packageId, 1, "CARD");

        when(coinPurchaseService.buy(eq("user@example.com"), any(BuyCoinPackageRequest.class)))
                .thenThrow(new CoinPackageNotFoundException("Paquete de monedas no encontrado o inactivo: " + packageId));

        mockMvc.perform(post("/api/v1/billing/coin-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Paquete de monedas no encontrado")));
    }

    // Extra: campos obligatorios ausentes → 400 Validation failed
    @Test
    @WithMockUser(username = "user@example.com")
    void buy_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/billing/coin-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    // Extra: sin autenticación → 401 Unauthorized
    @Test
    void buy_withoutAuth_returns401() throws Exception {
        BuyCoinPackageRequest request = buildRequest(UUID.randomUUID(), 1, "CARD");

        mockMvc.perform(post("/api/v1/billing/coin-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private BuyCoinPackageRequest buildRequest(UUID packageId, int quantity, String paymentMethod) {
        BuyCoinPackageRequest req = new BuyCoinPackageRequest();
        req.setCoinPackageId(packageId);
        req.setQuantity(quantity);
        req.setPaymentMethod(paymentMethod);
        return req;
    }

    private CoinPurchaseResponse buildPurchaseResponse(UUID packageId, int quantity, int totalCoins, int walletBalance) {
        User user = new User();
        user.setId(UUID.randomUUID());

        CoinPackage coinPackage = CoinPackage.builder()
                .id(packageId)
                .name("PRO")
                .coins(totalCoins / quantity)
                .price(new BigDecimal("12.00"))
                .active(true)
                .build();

        CoinPurchase purchase = CoinPurchase.builder()
                .id(UUID.randomUUID())
                .user(user)
                .coinPackage(coinPackage)
                .quantity(quantity)
                .totalAmount(coinPackage.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .status(CoinPurchaseStatus.COMPLETED)
                .build();
        // set createdAt via reflection would be needed; using a workaround via setter
        // The @PrePersist sets createdAt when persisted; in tests we simulate the built object
        // CoinPurchaseResponse handles null createdAt gracefully

        return new CoinPurchaseResponse(purchase, walletBalance);
    }
}

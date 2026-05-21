package com.mentoredu.billing.controller;

import com.mentoredu.billing.dto.BuyCoinPackageRequest;
import com.mentoredu.billing.dto.CoinPurchaseResponse;
import com.mentoredu.billing.service.ICoinPurchaseService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Billing — Monedas", description = "Compra de paquetes de monedas (US24).")
@SecurityRequirement(name = "bearerAuth")
public class CoinPurchaseController {

    private final ICoinPurchaseService coinPurchaseService;

    // -------------------------------------------------------------------------
    // US24 — Buy coin package
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/billing/coin-purchases")
    @Operation(
        summary = "US24 - Comprar paquete de monedas",
        description = "Registra la compra de uno o varios paquetes de monedas. "
            + "El pago se simula como aprobado; las monedas se acreditan al wallet del usuario. "
            + "Si el wallet no existe se crea automáticamente (RN-28). "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<CoinPurchaseResponse> buy(@Valid @RequestBody BuyCoinPackageRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coinPurchaseService.buy(auth.getName(), request));
    }
}

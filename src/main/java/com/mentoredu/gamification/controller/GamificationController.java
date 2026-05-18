package com.mentoredu.gamification.controller;

import com.mentoredu.gamification.dto.CoinsRequest;
import com.mentoredu.gamification.dto.CoinsResponse;
import com.mentoredu.gamification.dto.PointsResponse;
import com.mentoredu.gamification.service.IGamificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification/users/{userId}")
@RequiredArgsConstructor
@Tag(name = "Gamificación", description = "Consulta de puntos, monedas y nivel de usuario (EP-10). "
        + "Los endpoints de escritura (earn points, earn badges) se implementan internamente por eventos del sistema (RN-31).")
@SecurityRequirement(name = "bearerAuth")
public class GamificationController {

    private final IGamificationService gamificationService;

    @GetMapping("/points")
    @Operation(
        summary = "Consultar puntos de experiencia del usuario",
        description = "Devuelve el total de puntos de experiencia acumulados. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<PointsResponse> getPoints(@PathVariable UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(gamificationService.getPoints(userId));
    }

    @GetMapping("/coins")
    @Operation(
        summary = "Consultar saldo de monedas del usuario",
        description = "Devuelve el saldo actual de monedas virtuales. "
            + "Devuelve 0 si el usuario aún no ha realizado ninguna compra de monedas. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<CoinsResponse> getCoins(@PathVariable UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(gamificationService.getCoins(userId));
    }

    @PostMapping("/coins/redeem")
    @Operation(
        summary = "Canjear monedas",
        description = "Descuenta monedas del saldo del usuario. Usado internamente al descargar recursos PREMIUM. "
            + "Falla con 400 si el saldo es insuficiente. "
            + "Requiere autenticación JWT."
    )
    public ResponseEntity<CoinsResponse> redeemCoins(@PathVariable UUID userId,
                                                      @RequestBody CoinsRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(gamificationService.redeemCoins(userId, request));
    }
}

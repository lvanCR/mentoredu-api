package com.mentoredu.billing.controller;

import com.mentoredu.billing.dto.CoinPackageResponse;
import com.mentoredu.billing.dto.PlanResponse;
import com.mentoredu.billing.repository.CoinPackageRepository;
import com.mentoredu.billing.repository.PlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing — Catálogo", description = "Consulta pública de planes y paquetes de monedas (sin autenticación).")
public class BillingCatalogController {

    private final PlanRepository planRepository;
    private final CoinPackageRepository coinPackageRepository;

    @GetMapping("/plans")
    @Operation(
        summary = "Listar planes disponibles",
        description = "Devuelve todos los planes de suscripción disponibles. Endpoint público, no requiere autenticación."
    )
    public ResponseEntity<List<PlanResponse>> getPlans() {
        List<PlanResponse> plans = planRepository.findAll().stream()
                .map(PlanResponse::new)
                .toList();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/coin-packages")
    @Operation(
        summary = "Listar paquetes de monedas activos",
        description = "Devuelve todos los paquetes de monedas activos. Endpoint público, no requiere autenticación."
    )
    public ResponseEntity<List<CoinPackageResponse>> getCoinPackages() {
        List<CoinPackageResponse> packages = coinPackageRepository.findByActiveTrue().stream()
                .map(CoinPackageResponse::new)
                .toList();
        return ResponseEntity.ok(packages);
    }
}

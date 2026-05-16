package com.mentoredu.verification.controller;

import com.mentoredu.verification.dto.SubmitVerificationRequest;
import com.mentoredu.verification.dto.VerificationRequestResponse;
import com.mentoredu.verification.service.IVerificationService;
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
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
@Tag(name = "Verificación", description = "Solicitudes de verificación de identidad para docentes y academias (US21).")
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final IVerificationService verificationService;

    @PostMapping
    @Operation(summary = "US21 - Enviar solicitud de verificación")
    public ResponseEntity<VerificationRequestResponse> submit(
            @Valid @RequestBody SubmitVerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(verificationService.submit(userDetails.getUsername(), request));
    }

    @GetMapping("/me")
    @Operation(summary = "US21 - Consultar estado de mis solicitudes")
    public ResponseEntity<List<VerificationRequestResponse>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(verificationService.getMyRequests(userDetails.getUsername()));
    }
}

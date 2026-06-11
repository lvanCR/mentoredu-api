package com.mentoredu.contact.controller;

import com.mentoredu.auth.service.IEmailService;
import com.mentoredu.contact.dto.ContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Formulario de contacto desde la landing page")
public class ContactController {

    private final IEmailService emailService;

    @PostMapping
    @Operation(summary = "Recibir consulta desde la landing page")
    public ResponseEntity<Void> contact(@Valid @RequestBody ContactRequest req) {
        emailService.sendContactNotification(
            req.name(), req.email(), req.phone(), req.category(), req.institution()
        );
        return ResponseEntity.accepted().build();
    }
}

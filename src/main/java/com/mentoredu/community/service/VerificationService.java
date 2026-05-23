package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.event.VerificationProcessedEvent;
import com.mentoredu.community.exception.DuplicateVerificationException;
import com.mentoredu.community.exception.VerificationNotFoundException;
import com.mentoredu.community.model.VerificationDoc;
import com.mentoredu.community.model.VerificationRequest;
import com.mentoredu.community.repository.VerificationDocRepository;
import com.mentoredu.community.repository.VerificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService implements IVerificationService {

    private final VerificationRequestRepository verificationRepository;
    private final VerificationDocRepository     docRepository;
    private final UserRepository                userRepository;
    private final ApplicationEventPublisher     eventPublisher;

    @Override
    @Transactional
    public VerificationResponse submit(CreateVerificationRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userEmail));

        if (verificationRepository.existsByUserIdAndStatus(user.getId(), "PENDING")) {
            throw new DuplicateVerificationException("Ya tienes una solicitud de verificación pendiente");
        }

        VerificationRequest vr = VerificationRequest.builder()
                .user(user)
                .entityType(request.getEntityType())
                .status("PENDING")
                .build();

        VerificationRequest saved = verificationRepository.save(vr);

        List<VerificationDoc> docs = request.getDocuments().stream()
                .map(d -> VerificationDoc.builder()
                        .request(saved)
                        .documentType(d.getDocumentType())
                        .fileUrl(d.getFileUrl())
                        .build())
                .toList();
        docRepository.saveAll(docs);

        // Recargar para incluir documentos
        VerificationRequest withDocs = verificationRepository.findById(saved.getId())
                .orElseThrow();

        return new VerificationResponse(withDocs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationResponse> getMyRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userEmail));

        return verificationRepository.findByUserId(user.getId())
                .stream().map(VerificationResponse::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VerificationResponse> getAllRequests(int page, int size) {
        return PagedResponse.from(
                verificationRepository.findAll(PageRequest.of(page, size)),
                VerificationResponse::new);
    }

    @Override
    @Transactional
    public VerificationResponse review(UUID requestId, ReviewVerificationRequest request, String reviewerEmail) {
        VerificationRequest vr = verificationRepository.findById(requestId)
                .orElseThrow(() -> new VerificationNotFoundException("Solicitud no encontrada: " + requestId));

        if (!"PENDING".equals(vr.getStatus())) {
            throw new DuplicateVerificationException("La solicitud ya fue procesada con estado: " + vr.getStatus());
        }

        // RN-17: el rechazo requiere notas obligatorias
        if ("REJECTED".equals(request.getAction()) &&
                (request.getNotes() == null || request.getNotes().isBlank())) {
            throw new IllegalArgumentException("El rechazo requiere una razón (notes) obligatoria (RN-17)");
        }

        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + reviewerEmail));

        vr.setStatus(request.getAction());
        vr.setNotes(request.getNotes());
        vr.setReviewedBy(reviewer);
        vr.setReviewedAt(LocalDateTime.now());

        VerificationRequest saved = verificationRepository.save(vr);

        eventPublisher.publishEvent(new VerificationProcessedEvent(
                saved.getId(),
                saved.getUser().getId(),
                saved.getEntityType(),
                saved.getStatus(),
                saved.getNotes()
        ));

        return new VerificationResponse(saved);
    }
}

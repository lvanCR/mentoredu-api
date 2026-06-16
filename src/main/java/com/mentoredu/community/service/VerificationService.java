package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.event.VerificationProcessedEvent;
import com.mentoredu.community.exception.DuplicateVerificationException;
import com.mentoredu.community.exception.VerificationNotFoundException;
import com.mentoredu.community.model.VerificationDoc;
import com.mentoredu.community.model.VerificationRequest;
import com.mentoredu.community.model.VerificationStatus;
import com.mentoredu.community.repository.VerificationDocRepository;
import com.mentoredu.community.repository.VerificationRequestRepository;
import com.mentoredu.catalog.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService implements IVerificationService {

    private final VerificationRequestRepository verificationRepository;
    private final VerificationDocRepository     docRepository;
    private final UserService                   userService;
    private final UniversityRepository          universityRepository;
    private final ApplicationEventPublisher     eventPublisher;

    @Override
    @Transactional
    public VerificationResponse submit(CreateVerificationRequest request, String userEmail) {
        User user = userService.findByEmailOrThrow(userEmail);

        if (verificationRepository.existsByUserIdAndStatus(user.getId(), VerificationStatus.PENDING)) {
            throw new DuplicateVerificationException("Ya tienes una solicitud de verificación pendiente");
        }

        if (request.getUniversityId() != null && !universityRepository.existsById(request.getUniversityId())) {
            throw new IllegalArgumentException("Valor no encontrado en catalogo: universityId=" + request.getUniversityId());
        }

        VerificationRequest vr = VerificationRequest.builder()
                .user(user)
                .entityType(request.getEntityType())
                .universityId(request.getUniversityId())
                .status(VerificationStatus.PENDING)
                .build();

        VerificationRequest saved = verificationRepository.save(vr);
        log.info("Verification request {} submitted by user {} (entityType={})",
                saved.getId(), user.getId(), request.getEntityType());

        List<VerificationDoc> docs = request.getDocuments().stream()
                .map(d -> VerificationDoc.builder()
                        .request(saved)
                        .documentType(d.getDocumentType())
                        .fileUrl(d.getFileUrl())
                        .build())
                .toList();
        docRepository.saveAll(docs);
        saved.setDocs(docs);

        return new VerificationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VerificationResponse> getMyRequests(String userEmail, int page, int size) {
        User user = userService.findByEmailOrThrow(userEmail);
        return PagedResponse.from(
                verificationRepository.findByUserIdOrderBySubmittedAtDesc(user.getId(), PagedResponse.toPageRequest(page, size)),
                VerificationResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VerificationResponse> getAllRequests(VerificationStatus status, int page, int size) {
        var pageable = PagedResponse.toPageRequest(page, size);
        var page_ = (status != null)
                ? verificationRepository.findByStatusOrderBySubmittedAtDesc(status, pageable)
                : verificationRepository.findAllByOrderBySubmittedAtDesc(pageable);
        return PagedResponse.from(page_, VerificationResponse::new);
    }

    @Override
    @Transactional
    public VerificationResponse review(UUID requestId, ReviewVerificationRequest request, String reviewerEmail) {
        VerificationRequest vr = verificationRepository.findById(requestId)
                .orElseThrow(() -> new VerificationNotFoundException("Solicitud no encontrada: " + requestId));

        if (VerificationStatus.PENDING != vr.getStatus()) {
            throw new DuplicateVerificationException("La solicitud ya fue procesada con estado: " + vr.getStatus().name());
        }

        // RN-17: el rechazo requiere notas obligatorias
        if (VerificationStatus.REJECTED == request.getAction() &&
                (request.getNotes() == null || request.getNotes().isBlank())) {
            throw new IllegalArgumentException("El rechazo requiere una razón (notes) obligatoria (RN-17)");
        }

        User reviewer = userService.findByEmailOrThrow(reviewerEmail);

        vr.setStatus(request.getAction());
        vr.setNotes(request.getNotes());
        vr.setReviewedBy(reviewer);
        vr.setReviewedAt(LocalDateTime.now());

        VerificationRequest saved = verificationRepository.save(vr);

        log.info("Verification request {} {} for user {} (entityType={})",
                saved.getId(), request.getAction(), saved.getUser().getId(), saved.getEntityType());

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

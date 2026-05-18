package com.mentoredu.verification.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.forum.exception.UserNotFoundException;
import com.mentoredu.verification.dto.SubmitVerificationRequest;
import com.mentoredu.verification.dto.VerificationRequestResponse;
import com.mentoredu.verification.exception.DuplicateVerificationRequestException;
import com.mentoredu.verification.exception.UnauthorizedVerificationException;
import com.mentoredu.verification.model.VerificationDocument;
import com.mentoredu.verification.model.VerificationRequest;
import com.mentoredu.verification.model.enums.EntityType;
import com.mentoredu.verification.model.enums.VerificationStatus;
import com.mentoredu.verification.repository.VerificationDocumentRepository;
import com.mentoredu.verification.repository.VerificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationService implements IVerificationService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final VerificationDocumentRepository verificationDocumentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VerificationRequestResponse submit(String userEmail, SubmitVerificationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        // RN-25: TEACHER role → entityType TEACHER; ACADEMY role → entityType ACADEMY
        String roleName = user.getRole().getName();
        EntityType requiredEntityType = switch (roleName) {
            case "TEACHER" -> EntityType.TEACHER;
            case "ACADEMY" -> EntityType.ACADEMY;
            default -> throw new UnauthorizedVerificationException(
                    "Solo docentes y academias pueden solicitar verificación");
        };

        if (!request.getEntityType().equals(requiredEntityType)) {
            throw new UnauthorizedVerificationException(
                    "El tipo de entidad no corresponde a tu rol. Tu rol permite: " + requiredEntityType.name());
        }

        // RN-24: solo una solicitud activa (PENDING) por entidad
        if (verificationRequestRepository.existsByUserIdAndEntityTypeAndStatus(
                user.getId(), request.getEntityType(), VerificationStatus.PENDING)) {
            throw new DuplicateVerificationRequestException(
                    "Ya tienes una solicitud de verificación pendiente para " + request.getEntityType().name());
        }

        VerificationRequest verificationRequest = VerificationRequest.builder()
                .user(user)
                .entityType(request.getEntityType())
                .status(VerificationStatus.PENDING)
                .build();

        VerificationRequest saved = verificationRequestRepository.save(verificationRequest);

        // RN-23: toda solicitud requiere documento válido
        VerificationDocument document = VerificationDocument.builder()
                .request(saved)
                .documentType(request.getDocumentType())
                .fileUrl(request.getFileUrl())
                .build();

        VerificationDocument savedDocument = verificationDocumentRepository.save(document);

        return new VerificationRequestResponse(saved, savedDocument);
    }

    @Override
    public List<VerificationRequestResponse> getMyRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + userEmail));

        return verificationRequestRepository
                .findByUserIdOrderBySubmittedAtDesc(user.getId())
                .stream()
                .map(req -> {
                    List<VerificationDocument> docs = verificationDocumentRepository.findByRequestId(req.getId());
                    VerificationDocument doc = docs.isEmpty() ? null : docs.get(0);
                    return new VerificationRequestResponse(req, doc);
                })
                .toList();
    }
}

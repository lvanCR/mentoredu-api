package com.mentoredu.library.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.billing.model.enums.SubscriptionStatus;
import com.mentoredu.billing.repository.SubscriptionRepository;
import com.mentoredu.gamification.repository.CoinWalletRepository;
import com.mentoredu.library.dto.DownloadResult;
import com.mentoredu.library.dto.PublishResourceRequest;
import com.mentoredu.library.dto.ResourceResponse;
import com.mentoredu.library.exception.DuplicateResourceException;
import com.mentoredu.library.exception.ResourceAccessDeniedException;
import com.mentoredu.library.exception.ResourceFileNotFoundException;
import com.mentoredu.library.exception.ResourceNotFoundException;
import com.mentoredu.library.exception.ResourceNotAvailableException;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.model.DownloadLog;
import com.mentoredu.library.repository.AcademicResourceRepository;
import com.mentoredu.library.repository.DownloadLogRepository;
import com.mentoredu.library.repository.ResourceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService implements IResourceService {

    private static final Set<String> VALID_TYPES =
            Set.of("EXAM", "SOLUTION", "NOTES", "PRACTICE", "VIDEO", "OTHER");
    private static final Set<String> VALID_SEARCH_VISIBILITIES =
            Set.of("PUBLIC", "PREMIUM");

    private final AcademicResourceRepository resourceRepository;
    private final ResourceFileRepository resourceFileRepository;
    private final DownloadLogRepository downloadLogRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CoinWalletRepository coinWalletRepository;

    // -------------------------------------------------------------------------
    // US13 — Register resource metadata
    // -------------------------------------------------------------------------

    @Override
    public ResourceResponse publish(PublishResourceRequest request, String authorEmail) {
        var author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

        resourceFileRepository.findById(request.getFileId())
                .orElseThrow(() -> new ResourceFileNotFoundException(
                        "Resource file not found: " + request.getFileId()));

        if (resourceRepository.existsByFileId(request.getFileId())) {
            throw new DuplicateResourceException(
                    "A resource already exists for file: " + request.getFileId());
        }

        String visibility = (request.getVisibility() != null && !request.getVisibility().isBlank())
                ? request.getVisibility() : "PUBLIC";

        AcademicResource resource = AcademicResource.builder()
                .title(request.getTitle())
                .type(request.getType())
                .visibility(visibility)
                .description(request.getDescription())
                .subjectId(request.getSubjectId())
                .institutionId(request.getInstitutionId())
                .fileId(request.getFileId())
                .year(request.getYear())
                .examCycle(request.getExamCycle())
                .author(author)
                .build();

        return new ResourceResponse(resourceRepository.save(resource));
    }

    // -------------------------------------------------------------------------
    // US14 — Search resources by filters
    // -------------------------------------------------------------------------

    @Override
    public List<ResourceResponse> search(String query, String type, String visibility,
                                         UUID institutionId, UUID subjectId, Integer year) {
        String cleanType = validateAndNormalizeType(type);
        String cleanVisibility = validateAndNormalizeVisibility(visibility);
        validateYear(year);

        return resourceRepository.search(clean(query), cleanType, cleanVisibility, institutionId, subjectId, year)
                .stream()
                .map(ResourceResponse::new)
                .toList();
    }

    // -------------------------------------------------------------------------
    // US15 — Get resource metadata by ID
    // -------------------------------------------------------------------------

    @Override
    public ResourceResponse getById(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .map(ResourceResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));
    }

    // -------------------------------------------------------------------------
    // US15 — Download academic resource (stream file)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public DownloadResult downloadResource(UUID resourceId, String userEmail) {
        AcademicResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));

        checkAccessPermission(resource, user);

        var file = resourceFileRepository.findById(resource.getFileId())
                .orElseThrow(() -> new ResourceNotAvailableException(
                        "File record not found for resource: " + resourceId));

        Path filePath = Paths.get(file.getFileUrl());
        if (!Files.exists(filePath)) {
            throw new ResourceNotAvailableException(
                    "File is temporarily unavailable: " + file.getFileName());
        }

        downloadLogRepository.save(DownloadLog.builder()
                .user(user)
                .resource(resource)
                .downloadedAt(LocalDateTime.now())
                .build());

        return new DownloadResult(new FileSystemResource(filePath), file.getFileName());
    }

    // -------------------------------------------------------------------------
    // Access control (RN-15)
    // -------------------------------------------------------------------------

    private void checkAccessPermission(AcademicResource resource, User user) {
        switch (resource.getVisibility()) {
            case "PRIVATE" -> {
                if (!resource.getAuthor().getId().equals(user.getId())) {
                    throw new ResourceAccessDeniedException(
                            "This resource is private and only accessible to its author.");
                }
            }
            case "PREMIUM" -> {
                boolean hasActiveSubscription = subscriptionRepository
                        .findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                        .map(sub -> sub.getEndsAt() != null && sub.getEndsAt().isAfter(LocalDateTime.now()))
                        .orElse(false);

                boolean hasCoins = coinWalletRepository.findById(user.getId())
                        .map(wallet -> wallet.getBalance() != null && wallet.getBalance() > 0)
                        .orElse(false);

                if (!hasActiveSubscription && !hasCoins) {
                    throw new ResourceAccessDeniedException(
                            "This resource requires a premium subscription or coin balance to download.");
                }
            }
            default -> { /* PUBLIC — no restriction */ }
        }
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    private String validateAndNormalizeType(String type) {
        if (type == null || type.isBlank()) return null;
        String upper = type.trim().toUpperCase();
        if (!VALID_TYPES.contains(upper)) {
            throw new IllegalArgumentException(
                    "Invalid resource type: '" + type + "'. Allowed values: EXAM, SOLUTION, NOTES, PRACTICE, VIDEO, OTHER");
        }
        return upper;
    }

    private String validateAndNormalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) return null;
        String upper = visibility.trim().toUpperCase();
        if (!VALID_SEARCH_VISIBILITIES.contains(upper)) {
            throw new IllegalArgumentException(
                    "Invalid visibility filter: '" + visibility + "'. Allowed values for search: PUBLIC, PREMIUM");
        }
        return upper;
    }

    private void validateYear(Integer year) {
        if (year != null && (year < 1900 || year > 2099)) {
            throw new IllegalArgumentException(
                    "Year must be between 1900 and 2099. Received: " + year);
        }
    }

    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

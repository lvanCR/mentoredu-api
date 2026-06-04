package com.mentoredu.pedagogy.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.config.PagedResponse;
import com.mentoredu.library.model.Resource;
import com.mentoredu.library.repository.ResourceRepository;
import com.mentoredu.pedagogy.dto.MySolutionSummaryResponse;
import com.mentoredu.pedagogy.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.pedagogy.dto.ReceivedSolutionResponse;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;
import com.mentoredu.pedagogy.event.SolutionSubmittedEvent;
import com.mentoredu.pedagogy.exception.DuplicateSolutionException;
import com.mentoredu.pedagogy.exception.SolutionAccessDeniedException;
import com.mentoredu.pedagogy.exception.SolutionNotFoundException;
import com.mentoredu.pedagogy.model.FeedbackEntry;
import com.mentoredu.pedagogy.model.Solution;
import com.mentoredu.pedagogy.model.SolutionStatus;
import com.mentoredu.pedagogy.repository.FeedbackEntryRepository;
import com.mentoredu.pedagogy.repository.SolutionRepository;
import com.mentoredu.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolutionService implements ISolutionService {

    private final SolutionRepository solutionRepository;
    private final FeedbackEntryRepository feedbackEntryRepository;
    private final ResourceRepository resourceRepository;
    private final UserService    userService;
    private final ResourceAuthorizationService resourceAuthorizationService;
    private final ApplicationEventPublisher eventPublisher;
    private final FeedbackService feedbackService;

    @Override
    @Transactional
    public SolutionResponse submit(UUID resourceId, SubmitSolutionRequest request, String studentEmail) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));
        if (!resource.isAceptaResoluciones())
            throw new SolutionAccessDeniedException("Este recurso no acepta resoluciones");
        User student = userService.findByEmailOrThrow(studentEmail);
        if (!"STUDENT".equals(student.getRole().getName()))
            throw new SolutionAccessDeniedException("Solo los estudiantes pueden enviar resoluciones");
        if (solutionRepository.existsByResourceIdAndStudentId(resourceId, student.getId()))
            throw new DuplicateSolutionException("Ya enviaste una resolución para este ejercicio");
        if (request.fileUrl() == null && request.content() == null)
            throw new IllegalArgumentException("Debes proporcionar un archivo (fileUrl) o contenido (content)");
        Solution solution = Solution.builder()
            .resourceId(resourceId)
            .student(student)
            .fileUrl(request.fileUrl())
            .content(request.content())
            .status(SolutionStatus.SUBMITTED)
            .build();
        Solution saved = solutionRepository.save(solution);
        eventPublisher.publishEvent(new SolutionSubmittedEvent(
                saved.getId(), resourceId, resource.getAuthor().getId(), student.getId()));
        return SolutionResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MySolutionWithFeedbackResponse getMyWithFeedback(UUID resourceId, String studentEmail) {
        User student = userService.findByEmailOrThrow(studentEmail);
        Solution solution = solutionRepository.findByResourceIdAndStudentId(resourceId, student.getId())
            .orElseThrow(() -> new SolutionNotFoundException("No has enviado resolución para este ejercicio"));
        var feedbackEntry = feedbackEntryRepository.findBySolutionId(solution.getId()).orElse(null);
        var feedbackResponse = feedbackEntry != null ? feedbackService.enriched(feedbackEntry) : null;
        return new MySolutionWithFeedbackResponse(SolutionResponse.from(solution), feedbackResponse);
    }

    @Override
    public SolutionResponse getById(UUID resourceId, UUID solutionId, String requesterEmail) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));
        User requester = userService.findByEmailOrThrow(requesterEmail);
        if (!resourceAuthorizationService.isAuthorizedForResource(resource, requester))
            throw new SolutionAccessDeniedException("Solo el autor del ejercicio puede ver las resoluciones");
        Solution solution = solutionRepository.findById(solutionId)
            .orElseThrow(() -> new SolutionNotFoundException("Resolución no encontrada: " + solutionId));
        if (!solution.getResourceId().equals(resourceId))
            throw new SolutionNotFoundException("La resolución no pertenece a este ejercicio");
        return SolutionResponse.from(solution);
    }

    @Override
    public List<SolutionResponse> listByResource(UUID resourceId, String requesterEmail) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado: " + resourceId));
        User requester = userService.findByEmailOrThrow(requesterEmail);
        if (!resourceAuthorizationService.isAuthorizedForResource(resource, requester))
            throw new SolutionAccessDeniedException("Solo el autor del ejercicio puede ver las resoluciones");
        return solutionRepository.findByResourceId(resourceId).stream()
            .map(SolutionResponse::from).toList();
    }

    // ── GET /solutions/mine ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MySolutionSummaryResponse> getMySolutions(String studentEmail, int page, int size) {
        User student = userService.findByEmailOrThrow(studentEmail);
        Page<Solution> solutionPage = solutionRepository.findByStudentIdPaged(
                student.getId(), PagedResponse.toPageRequest(page, size));

        List<UUID> resourceIds = solutionPage.map(Solution::getResourceId).toList();
        Map<UUID, Resource> resourceMap = resourceRepository.findAllById(resourceIds)
                .stream().collect(Collectors.toMap(Resource::getId, r -> r));

        List<UUID> solutionIds = solutionPage.map(Solution::getId).toList();
        Map<UUID, FeedbackEntry> feedbackMap = feedbackEntryRepository.findBySolution_IdIn(solutionIds)
                .stream().collect(Collectors.toMap(f -> f.getSolution().getId(), f -> f));

        return PagedResponse.from(solutionPage, s ->
                MySolutionSummaryResponse.of(s, resourceMap.get(s.getResourceId()),
                        feedbackMap.get(s.getId())));
    }

    // ── GET /solutions/received ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReceivedSolutionResponse> getReceivedSolutions(String teacherEmail, int page, int size) {
        User teacher = userService.findByEmailOrThrow(teacherEmail);
        Page<Solution> solutionPage = solutionRepository.findReceivedByAuthorId(
                teacher.getId(), PagedResponse.toPageRequest(page, size));

        List<UUID> resourceIds = solutionPage.map(Solution::getResourceId).toList();
        Map<UUID, Resource> resourceMap = resourceRepository.findAllById(resourceIds)
                .stream().collect(Collectors.toMap(Resource::getId, r -> r));

        List<UUID> solutionIds = solutionPage.map(Solution::getId).toList();
        Map<UUID, FeedbackEntry> feedbackMap = feedbackEntryRepository.findBySolution_IdIn(solutionIds)
                .stream().collect(Collectors.toMap(f -> f.getSolution().getId(), f -> f));

        return PagedResponse.from(solutionPage, s ->
                ReceivedSolutionResponse.of(s, resourceMap.get(s.getResourceId()),
                        feedbackMap.get(s.getId())));
    }
}

package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;
import com.mentoredu.community.event.AssociationResolvedEvent;
import com.mentoredu.community.exception.AssociationNotFoundException;
import com.mentoredu.community.exception.DuplicateAssociationException;
import com.mentoredu.community.model.TeacherAcademyLink;
import com.mentoredu.community.repository.TeacherAcademyLinkRepository;
import com.mentoredu.profile.model.AcademyProfile;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.repository.AcademyProfileRepository;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssociationService implements IAssociationService {

    private final TeacherAcademyLinkRepository linkRepository;
    private final ProfileRepository profileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final AcademyProfileRepository academyProfileRepository;
    private final UserService    userService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AssociationResponse requestAssociation(CreateAssociationRequest request, String teacherEmail) {
        Profile profile = resolveProfile(teacherEmail);

        if (!teacherProfileRepository.existsById(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo docentes pueden solicitar asociaciones");
        }

        AcademyProfile academy = academyProfileRepository.findById(request.academyProfileId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Academia no encontrada: " + request.academyProfileId()));

        if (linkRepository.findByTeacherProfileIdAndAcademyProfileId(
                profile.getId(), academy.getProfileId()).isPresent()) {
            throw new DuplicateAssociationException("Ya existe una solicitud de asociación con esta academia");
        }

        TeacherAcademyLink link = TeacherAcademyLink.builder()
                .teacherProfileId(profile.getId())
                .academyProfileId(academy.getProfileId())
                .status("PENDING")
                .build();

        return new AssociationResponse(linkRepository.save(link));
    }

    @Override
    public List<AssociationResponse> getMyAssociations(String teacherEmail) {
        Profile profile = resolveProfile(teacherEmail);
        return linkRepository.findByTeacherProfileId(profile.getId())
                .stream().map(AssociationResponse::new).toList();
    }

    @Override
    public List<AssociationResponse> getAcademyRequests(String academyEmail) {
        Profile profile = resolveProfile(academyEmail);
        if (!academyProfileRepository.existsById(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo academias pueden ver solicitudes recibidas");
        }
        return linkRepository.findByAcademyProfileId(profile.getId())
                .stream().map(AssociationResponse::new).toList();
    }

    @Override
    @Transactional
    public AssociationResponse acceptAssociation(UUID linkId, String academyEmail) {
        return resolveAssociation(linkId, "ACCEPTED", academyEmail);
    }

    @Override
    @Transactional
    public AssociationResponse rejectAssociation(UUID linkId, String academyEmail) {
        return resolveAssociation(linkId, "REJECTED", academyEmail);
    }

    private AssociationResponse resolveAssociation(UUID id, String newStatus, String academyEmail) {
        Profile profile = resolveProfile(academyEmail);

        if (!academyProfileRepository.existsById(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo academias pueden resolver solicitudes");
        }

        TeacherAcademyLink link = linkRepository.findById(id)
                .orElseThrow(() -> new AssociationNotFoundException("Solicitud de asociación no encontrada: " + id));

        if (!link.getAcademyProfileId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para resolver esta solicitud");
        }

        if (!"PENDING".equals(link.getStatus())) {
            throw new DuplicateAssociationException("La solicitud ya fue resuelta con estado: " + link.getStatus());
        }

        link.setStatus(newStatus);
        link.setResolvedAt(LocalDateTime.now());
        TeacherAcademyLink saved = linkRepository.save(link);

        profileRepository.findById(saved.getTeacherProfileId()).ifPresent(teacherProfile ->
                eventPublisher.publishEvent(new AssociationResolvedEvent(
                        saved.getId(),
                        teacherProfile.getUserId(),
                        saved.getAcademyProfileId(),
                        saved.getStatus()
                ))
        );

        return new AssociationResponse(saved);
    }

    private Profile resolveProfile(String email) {
        User user = userService.findByEmailOrThrow(email);
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Perfil no encontrado para: " + email));
    }
}

package com.mentoredu.community.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.community.dto.AssociationResponse;
import com.mentoredu.community.dto.CreateAssociationRequest;
import com.mentoredu.community.event.AssociationResolvedEvent;
import com.mentoredu.community.exception.AcademyNotFoundException;
import com.mentoredu.community.exception.AcademyProfileRequiredException;
import com.mentoredu.community.exception.AssociationNotFoundException;
import com.mentoredu.community.exception.DuplicateAssociationException;
import com.mentoredu.community.exception.NotAssociationOwnerException;
import com.mentoredu.community.exception.TeacherProfileRequiredException;
import com.mentoredu.community.model.AssociationStatus;
import com.mentoredu.profile.exception.ProfileNotFoundException;
import com.mentoredu.community.model.TeacherAcademyLink;
import com.mentoredu.community.repository.TeacherAcademyLinkRepository;
import com.mentoredu.profile.model.AcademyProfile;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.repository.AcademyProfileRepository;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new TeacherProfileRequiredException("Solo docentes pueden solicitar asociaciones");
        }

        AcademyProfile academy = academyProfileRepository.findById(request.academyProfileId())
                .orElseThrow(() -> new AcademyNotFoundException("Academia no encontrada: " + request.academyProfileId()));

        if (linkRepository.findByTeacherProfileIdAndAcademyProfileId(
                profile.getId(), academy.getProfileId()).isPresent()) {
            throw new DuplicateAssociationException("Ya existe una solicitud de asociación con esta academia");
        }

        TeacherAcademyLink link = TeacherAcademyLink.builder()
                .teacherProfileId(profile.getId())
                .academyProfileId(academy.getProfileId())
                .status(AssociationStatus.PENDING)
                .build();

        return new AssociationResponse(linkRepository.save(link));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssociationResponse> getMyAssociations(String teacherEmail) {
        Profile profile = resolveProfile(teacherEmail);
        return linkRepository.findByTeacherProfileId(profile.getId())
                .stream().map(AssociationResponse::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssociationResponse> getAcademyRequests(String academyEmail) {
        Profile profile = resolveProfile(academyEmail);
        if (!academyProfileRepository.existsById(profile.getId())) {
            throw new AcademyProfileRequiredException("Solo academias pueden ver solicitudes recibidas");
        }
        return linkRepository.findByAcademyProfileId(profile.getId())
                .stream().map(AssociationResponse::new).toList();
    }

    @Override
    @Transactional
    public AssociationResponse acceptAssociation(UUID linkId, String academyEmail) {
        return resolveAssociation(linkId, AssociationStatus.ACCEPTED, academyEmail);
    }

    @Override
    @Transactional
    public AssociationResponse rejectAssociation(UUID linkId, String academyEmail) {
        return resolveAssociation(linkId, AssociationStatus.REJECTED, academyEmail);
    }

    private AssociationResponse resolveAssociation(UUID id, AssociationStatus newStatus, String academyEmail) {
        Profile profile = resolveProfile(academyEmail);

        if (!academyProfileRepository.existsById(profile.getId())) {
            throw new AcademyProfileRequiredException("Solo academias pueden resolver solicitudes");
        }

        TeacherAcademyLink link = linkRepository.findById(id)
                .orElseThrow(() -> new AssociationNotFoundException("Solicitud de asociación no encontrada: " + id));

        if (!link.getAcademyProfileId().equals(profile.getId())) {
            throw new NotAssociationOwnerException("No tienes permiso para resolver esta solicitud");
        }

        if (AssociationStatus.PENDING != link.getStatus()) {
            throw new DuplicateAssociationException("La solicitud ya fue resuelta con estado: " + link.getStatus().name());
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
                .orElseThrow(() -> new ProfileNotFoundException("Perfil no encontrado para el usuario: " + email));
    }
}

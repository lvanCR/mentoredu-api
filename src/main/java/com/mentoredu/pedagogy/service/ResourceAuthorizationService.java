package com.mentoredu.pedagogy.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.community.model.AssociationStatus;
import com.mentoredu.community.repository.TeacherAcademyLinkRepository;
import com.mentoredu.library.model.Resource;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.repository.AcademyProfileRepository;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * RN-10: puede acceder a resoluciones/feedback el autor directo del recurso
 * o una cuenta vinculada por TeacherAcademyLink en status=ACCEPTED.
 */
@Component
@RequiredArgsConstructor
public class ResourceAuthorizationService {

    private final TeacherProfileRepository teacherProfileRepository;
    private final AcademyProfileRepository academyProfileRepository;
    private final TeacherAcademyLinkRepository teacherAcademyLinkRepository;
    private final ProfileRepository profileRepository;

    public boolean isAuthorizedForResource(Resource resource, User requester) {
        if ("ADMIN".equals(requester.getRole().getName())) return true;
        if (resource.getAuthor().getId().equals(requester.getId())) return true;

        return authorizedResourceAuthorIds(requester).contains(resource.getAuthor().getId());
    }

    public List<UUID> authorizedResourceAuthorIds(User requester) {
        Set<UUID> authorIds = new LinkedHashSet<>();
        authorIds.add(requester.getId());

        String role = requester.getRole().getName();

        if ("TEACHER".equals(role)) {
            teacherProfileRepository.findByProfile_UserId(requester.getId()).ifPresent(teacherProfile -> {
                List<UUID> academyProfileIds = teacherAcademyLinkRepository
                    .findByTeacherProfileId(teacherProfile.getProfileId()).stream()
                    .filter(link -> AssociationStatus.ACCEPTED == link.getStatus())
                    .map(link -> link.getAcademyProfileId())
                    .toList();

                profileRepository.findAllById(academyProfileIds).stream()
                    .map(Profile::getUserId)
                    .forEach(authorIds::add);
            });
        }

        if ("ACADEMY".equals(role)) {
            academyProfileRepository.findByProfile_UserId(requester.getId()).ifPresent(academyProfile -> {
                List<UUID> teacherProfileIds = teacherAcademyLinkRepository
                    .findByAcademyProfileId(academyProfile.getProfileId()).stream()
                    .filter(link -> AssociationStatus.ACCEPTED == link.getStatus())
                    .map(link -> link.getTeacherProfileId())
                    .toList();

                profileRepository.findAllById(teacherProfileIds).stream()
                    .map(Profile::getUserId)
                    .forEach(authorIds::add);
            });
        }

        return List.copyOf(authorIds);
    }
}

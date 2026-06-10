package com.mentoredu.pedagogy.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.community.model.AssociationStatus;
import com.mentoredu.community.repository.TeacherAcademyLinkRepository;
import com.mentoredu.library.model.Resource;
import com.mentoredu.profile.repository.AcademyProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * RN-10: puede acceder a resoluciones/feedback el autor directo del recurso
 * o un TEACHER con TeacherAcademyLink en status=ACCEPTED a la academia autora.
 */
@Component
@RequiredArgsConstructor
public class ResourceAuthorizationService {

    private final TeacherProfileRepository teacherProfileRepository;
    private final AcademyProfileRepository academyProfileRepository;
    private final TeacherAcademyLinkRepository teacherAcademyLinkRepository;

    public boolean isAuthorizedForResource(Resource resource, User requester) {
        if (resource.getAuthor().getId().equals(requester.getId())) return true;

        var academyProfile = academyProfileRepository.findByProfile_UserId(resource.getAuthor().getId());
        if (academyProfile.isEmpty()) return false;

        var teacherProfile = teacherProfileRepository.findByProfile_UserId(requester.getId());
        if (teacherProfile.isEmpty()) return false;

        return teacherAcademyLinkRepository
            .findByTeacherProfileIdAndAcademyProfileId(
                teacherProfile.get().getProfileId(), academyProfile.get().getProfileId())
            .map(link -> AssociationStatus.ACCEPTED == link.getStatus())
            .orElse(false);
    }
}

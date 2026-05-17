package com.mentoredu.academy.service;

import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;
import com.mentoredu.academy.exception.AcademyAlreadyExistsException;
import com.mentoredu.academy.model.Academy;
import com.mentoredu.academy.repository.AcademyRepository;
import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.profile.exception.ProfileNotFoundException;
import com.mentoredu.profile.exception.WrongProfileTypeException;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.model.ProfileType;
import com.mentoredu.profile.repository.OrganizationProfileRepository;
import com.mentoredu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademyService implements IAcademyService {

    private final AcademyRepository              academyRepository;
    private final ProfileRepository              profileRepository;
    private final OrganizationProfileRepository  organizationProfileRepository;
    private final UserRepository                 userRepository;

    // -------------------------------------------------------------------------
    // US33 — Create academy
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AcademyResponse createAcademy(String email, CreateAcademyRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Profile not found for user: " + email));

        if (!ProfileType.ORGANIZATION.name().equals(profile.getProfileType())) {
            throw new WrongProfileTypeException(
                    "Account type is not ORGANIZATION. Current type: " + profile.getProfileType());
        }

        if (!organizationProfileRepository.existsById(profile.getId())) {
            throw new ProfileNotFoundException(
                    "Organization profile not found for user: " + email);
        }

        if (academyRepository.existsByNameAndOwnerProfileId(request.getName(), profile.getId())) {
            throw new AcademyAlreadyExistsException(
                    "An academy with this name already exists for this organization: " + request.getName());
        }

        Academy academy = Academy.builder()
                .ownerProfileId(profile.getId())
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .email(request.getEmail())
                .active(true)
                .build();

        return new AcademyResponse(academyRepository.save(academy));
    }
}

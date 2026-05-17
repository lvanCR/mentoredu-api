package com.mentoredu.academy.service;

import com.mentoredu.academy.dto.AcademyResponse;
import com.mentoredu.academy.dto.CreateAcademyRequest;
import com.mentoredu.academy.dto.CreateCycleRequest;
import com.mentoredu.academy.dto.CreateProgramRequest;
import com.mentoredu.academy.dto.CycleResponse;
import com.mentoredu.academy.dto.ProgramResponse;
import com.mentoredu.academy.exception.AcademyAlreadyExistsException;
import com.mentoredu.academy.exception.AcademyNotFoundException;
import com.mentoredu.academy.exception.CycleAlreadyExistsException;
import com.mentoredu.academy.exception.ProgramAlreadyExistsException;
import com.mentoredu.academy.model.Academy;
import com.mentoredu.academy.model.Cycle;
import com.mentoredu.academy.model.Program;
import com.mentoredu.academy.repository.AcademyRepository;
import com.mentoredu.academy.repository.CycleRepository;
import com.mentoredu.academy.repository.ProgramRepository;
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

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcademyService implements IAcademyService {

    private final AcademyRepository             academyRepository;
    private final ProgramRepository             programRepository;
    private final CycleRepository               cycleRepository;
    private final ProfileRepository             profileRepository;
    private final OrganizationProfileRepository organizationProfileRepository;
    private final UserRepository                userRepository;

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

    // -------------------------------------------------------------------------
    // US11 — Register academic offering: program
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ProgramResponse createProgram(String email, UUID academyId, CreateProgramRequest request) {
        Profile profile = resolveOrganizationProfile(email);
        Academy academy = resolveOwnedAcademy(academyId, profile.getId());

        if (programRepository.existsByNameAndAcademyId(request.getName(), academy.getId())) {
            throw new ProgramAlreadyExistsException(
                    "A program with this name already exists in the academy: " + request.getName());
        }

        Program program = Program.builder()
                .academyId(academy.getId())
                .name(request.getName())
                .modality(request.getModality())
                .intensity(request.getIntensity())
                .targetUniversity(request.getTargetUniversity())
                .build();

        return new ProgramResponse(programRepository.save(program));
    }

    // -------------------------------------------------------------------------
    // US11 — Register academic offering: cycle
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public CycleResponse createCycle(String email, UUID academyId, CreateCycleRequest request) {
        Profile profile = resolveOrganizationProfile(email);
        Academy academy = resolveOwnedAcademy(academyId, profile.getId());

        if (cycleRepository.existsByNameAndAcademyId(request.getName(), academy.getId())) {
            throw new CycleAlreadyExistsException(
                    "A cycle with this name already exists in the academy: " + request.getName());
        }

        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate   = LocalDate.parse(request.getEndDate());

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        Cycle cycle = Cycle.builder()
                .academyId(academy.getId())
                .name(request.getName())
                .cycleType(request.getCycleType())
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return new CycleResponse(cycleRepository.save(cycle));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Profile resolveOrganizationProfile(String email) {
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

        return profile;
    }

    private Academy resolveOwnedAcademy(UUID academyId, UUID ownerProfileId) {
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AcademyNotFoundException(
                        "Academy not found: " + academyId));

        if (!academy.getOwnerProfileId().equals(ownerProfileId)) {
            throw new AcademyNotFoundException(
                    "Academy not found or not owned by this organization: " + academyId);
        }

        return academy;
    }
}

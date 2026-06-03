package com.mentoredu.profile.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import com.mentoredu.catalog.repository.AreaRepository;
import com.mentoredu.catalog.repository.CareerRepository;
import com.mentoredu.catalog.repository.UniversityRepository;
import com.mentoredu.community.repository.FollowRepository;
import com.mentoredu.profile.dto.*;
import com.mentoredu.profile.exception.*;
import com.mentoredu.profile.model.*;
import com.mentoredu.profile.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final ProfileRepository            profileRepository;
    private final StudentProfileRepository     studentProfileRepository;
    private final TeacherProfileRepository     teacherProfileRepository;
    private final AcademyProfileRepository     academyProfileRepository;
    private final UserService                  userService;
    private final UniversityRepository         universityRepository;
    private final AreaRepository               areaRepository;
    private final CareerRepository             careerRepository;
    private final FollowRepository             followRepository;

    // -------------------------------------------------------------------------
    // US05 — Update common profile data
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userService.findByEmailOrThrow(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));

        profile.setDisplayName(request.getDisplayName());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getCity()      != null) profile.setCity(request.getCity());
        if (request.getBio()       != null) profile.setBio(request.getBio());

        return new ProfileResponse(profileRepository.save(profile));
    }

    // -------------------------------------------------------------------------
    // GET /profiles/me
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ProfileMeResponse getMyProfile(String email) {
        User user = userService.findByEmailOrThrow(email);

        // ADMIN / MODERATOR are provisioned directly in the DB with no profile record.
        // Return a synthetic response so they don't see a 404.
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return ProfileMeResponse.forSystemUser(user);
        }

        String type = profile.getProfileType();
        boolean complete = switch (type != null ? type : "") {
            case "STUDENT" -> studentProfileRepository.existsById(profile.getId());
            case "TEACHER" -> teacherProfileRepository.existsById(profile.getId());
            case "ACADEMY" -> academyProfileRepository.existsById(profile.getId());
            default        -> true; // system roles: base profile counts as complete
        };

        return new ProfileMeResponse(profile, complete);
    }

    // -------------------------------------------------------------------------
    // US06 — Create student profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public StudentProfileResponse createStudentProfile(String email, CreateStudentProfileRequest request) {
        User user = userService.findByEmailOrThrow(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));

        if (!ProfileType.STUDENT.name().equals(profile.getProfileType())) {
            throw new WrongProfileTypeException(
                    "Account type is not STUDENT. Current type: " + profile.getProfileType());
        }

        if (studentProfileRepository.existsById(profile.getId())) {
            throw new StudentProfileAlreadyExistsException(
                    "Student profile already exists for this account.");
        }

        validateCatalogIds(request.getTargetUniversityId(), request.getTargetAreaId(), request.getTargetCareerId());

        StudentProfile studentProfile = StudentProfile.builder()
                .profile(profile)
                .gradeLevel(request.getGradeLevel())
                .schoolName(request.getSchoolName())
                .studyShift(request.getStudyShift())
                .targetUniversityId(request.getTargetUniversityId())
                .targetAreaId(request.getTargetAreaId())
                .targetCareerId(request.getTargetCareerId())
                .build();

        return new StudentProfileResponse(studentProfileRepository.save(studentProfile));
    }

    // -------------------------------------------------------------------------
    // GET /profiles/student/{userId}
    // -------------------------------------------------------------------------

    @Override
    public StudentProfileResponse getStudentProfile(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userId));
        StudentProfile studentProfile = studentProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Student profile not found for user: " + userId));
        return new StudentProfileResponse(studentProfile);
    }

    // -------------------------------------------------------------------------
    // PATCH /profiles/student/me — Update student profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public StudentProfileResponse updateStudentProfile(String email, UpdateStudentProfileRequest request) {
        User user = userService.findByEmailOrThrow(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));

        StudentProfile studentProfile = studentProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Student profile not found for user: " + email));

        validateCatalogIds(request.getTargetUniversityId(), request.getTargetAreaId(), request.getTargetCareerId());

        if (request.getSchoolName()         != null) studentProfile.setSchoolName(request.getSchoolName());
        if (request.getGradeLevel()         != null) studentProfile.setGradeLevel(request.getGradeLevel());
        if (request.getStudyShift()         != null) studentProfile.setStudyShift(request.getStudyShift());
        if (request.getTargetUniversityId() != null) studentProfile.setTargetUniversityId(request.getTargetUniversityId());
        if (request.getTargetAreaId()       != null) studentProfile.setTargetAreaId(request.getTargetAreaId());
        if (request.getTargetCareerId()     != null) studentProfile.setTargetCareerId(request.getTargetCareerId());

        return new StudentProfileResponse(studentProfileRepository.save(studentProfile));
    }

    // -------------------------------------------------------------------------
    // Create teacher profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public TeacherProfileResponse createTeacherProfile(String email, CreateTeacherProfileRequest request) {
        User user = userService.findByEmailOrThrow(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));

        if (!ProfileType.TEACHER.name().equals(profile.getProfileType())) {
            throw new WrongProfileTypeException(
                    "Account type is not TEACHER. Current type: " + profile.getProfileType());
        }

        if (teacherProfileRepository.existsById(profile.getId())) {
            throw new TeacherProfileAlreadyExistsException(
                    "Teacher profile already exists for this account.");
        }

        TeacherProfile teacherProfile = TeacherProfile.builder()
                .profile(profile)
                .bioProfessional(request.getBioProfessional())
                .build();

        return new TeacherProfileResponse(teacherProfileRepository.save(teacherProfile));
    }

    // -------------------------------------------------------------------------
    // GET /profiles/teacher/me
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TeacherProfileResponse getTeacherProfile(String email) {
        User user = userService.findByEmailOrThrow(email);
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));
        TeacherProfile teacherProfile = teacherProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Teacher profile not found for user: " + email));
        return new TeacherProfileResponse(teacherProfile);
    }

    // -------------------------------------------------------------------------
    // Update teacher profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public TeacherProfileResponse updateTeacherProfile(String email, UpdateTeacherProfileRequest request) {
        User user = userService.findByEmailOrThrow(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));

        TeacherProfile teacherProfile = teacherProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Teacher profile not found for user: " + email));

        if (request.getBioProfessional() != null) teacherProfile.setBioProfessional(request.getBioProfessional());

        return new TeacherProfileResponse(teacherProfileRepository.save(teacherProfile));
    }

    // -------------------------------------------------------------------------
    // GET /profiles/{userId} — public profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(UUID userId, String callerEmail) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userId));

        long followerCount  = followRepository.countByFollowedId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        User caller       = userService.findByEmailOrThrow(callerEmail);
        boolean isFollowing       = followRepository.existsByFollowerIdAndFollowedId(caller.getId(), userId);
        boolean hasStudentProfile = "STUDENT".equals(profile.getProfileType())
                && studentProfileRepository.existsById(profile.getId());

        return new ProfileResponse(profile, followerCount, followingCount, isFollowing, hasStudentProfile);
    }

    // -------------------------------------------------------------------------
    // GET /profiles/academy/me
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public AcademyProfileResponse getAcademyProfile(String email) {
        User user = userService.findByEmailOrThrow(email);
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));
        AcademyProfile academyProfile = academyProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Academy profile not found for user: " + email));
        return AcademyProfileResponse.from(academyProfile);
    }

    // -------------------------------------------------------------------------
    // Create academy profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AcademyProfileResponse createAcademyProfile(String email, CreateAcademyProfileRequest request) {
        User user = userService.findByEmailOrThrow(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));

        if (!ProfileType.ACADEMY.name().equals(profile.getProfileType())) {
            throw new WrongProfileTypeException(
                    "Account type is not ACADEMY. Current type: " + profile.getProfileType());
        }

        if (academyProfileRepository.existsById(profile.getId())) {
            throw new AcademyProfileAlreadyExistsException(
                    "Academy profile already exists for this account.");
        }

        if (academyProfileRepository.existsByAcademyName(request.academyName())) {
            throw new AcademyNameAlreadyExistsException(
                    "An academy with this name already exists: " + request.academyName());
        }

        if (request.ruc() != null && academyProfileRepository.existsByRuc(request.ruc())) {
            throw new AcademyNameAlreadyExistsException(
                    "An academy with this RUC already exists: " + request.ruc());
        }

        AcademyProfile academyProfile = AcademyProfile.builder()
                .profile(profile)
                .academyName(request.academyName())
                .ruc(request.ruc())
                .website(request.website())
                .contactEmail(request.contactEmail())
                .build();

        return AcademyProfileResponse.from(academyProfileRepository.save(academyProfile));
    }

    // -------------------------------------------------------------------------
    // Update academy profile (US06)
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void validateCatalogIds(UUID universityId, UUID areaId, UUID careerId) {
        if (universityId != null && !universityRepository.existsById(universityId)) {
            throw new IllegalArgumentException("Valor no encontrado en catálogo: universityId=" + universityId);
        }
        if (areaId != null && !areaRepository.existsById(areaId)) {
            throw new IllegalArgumentException("Valor no encontrado en catálogo: areaId=" + areaId);
        }
        if (careerId != null && !careerRepository.existsById(careerId)) {
            throw new IllegalArgumentException("Valor no encontrado en catálogo: careerId=" + careerId);
        }
    }

    @Override
    @Transactional
    public AcademyProfileResponse updateAcademyProfile(String email, UpdateAcademyProfileRequest request) {
        User user = userService.findByEmailOrThrow(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + email));

        AcademyProfile academyProfile = academyProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Academy profile not found for user: " + email));

        if (request.getAcademyName() != null && !request.getAcademyName().isBlank()) {
            if (!Objects.equals(academyProfile.getAcademyName(), request.getAcademyName())
                    && academyProfileRepository.existsByAcademyName(request.getAcademyName())) {
                throw new AcademyNameAlreadyExistsException(
                        "An academy with this name already exists: " + request.getAcademyName());
            }
            academyProfile.setAcademyName(request.getAcademyName());
        }
        if (request.getWebsite()      != null) academyProfile.setWebsite(request.getWebsite());
        if (request.getContactEmail() != null) academyProfile.setContactEmail(request.getContactEmail());

        return AcademyProfileResponse.from(academyProfileRepository.save(academyProfile));
    }
}

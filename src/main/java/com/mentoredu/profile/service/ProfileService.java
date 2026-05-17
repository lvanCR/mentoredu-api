package com.mentoredu.profile.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.profile.dto.CreateStudentProfileRequest;
import com.mentoredu.profile.dto.CreateTeacherProfileRequest;
import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.TeacherProfileResponse;
import com.mentoredu.profile.dto.UpdateProfileRequest;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.dto.UpdateTeacherProfileRequest;
import com.mentoredu.profile.exception.ProfileAlreadyExistsException;
import com.mentoredu.profile.exception.ProfileNotFoundException;
import com.mentoredu.profile.exception.StudentProfileAlreadyExistsException;
import com.mentoredu.profile.exception.TeacherProfileAlreadyExistsException;
import com.mentoredu.profile.exception.WrongProfileTypeException;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.model.ProfileType;
import com.mentoredu.profile.model.StudentProfile;
import com.mentoredu.profile.model.TeacherProfile;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.StudentProfileRepository;
import com.mentoredu.profile.repository.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final ProfileRepository        profileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRepository           userRepository;

    // -------------------------------------------------------------------------
    // US04 — Select account type
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ProfileResponse selectAccountType(String email, SelectAccountTypeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (profileRepository.findByUserId(user.getId()).isPresent()) {
            throw new ProfileAlreadyExistsException(
                    "User already has a profile. Account type cannot be changed.");
        }

        String displayName = (user.getFirstName() + " " + user.getLastName()).trim();

        Profile profile = Profile.builder()
                .userId(user.getId())
                .displayName(displayName)
                .profileType(request.getProfileType().name())
                .build();

        return new ProfileResponse(profileRepository.save(profile));
    }

    // -------------------------------------------------------------------------
    // US05 — Update common profile data
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Profile not found for user: " + email));

        profile.setDisplayName(request.getDisplayName());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getCity()      != null) profile.setCity(request.getCity());
        if (request.getBio()       != null) profile.setBio(request.getBio());

        return new ProfileResponse(profileRepository.save(profile));
    }

    // -------------------------------------------------------------------------
    // US06 — Create student profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public StudentProfileResponse createStudentProfile(String email, CreateStudentProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Profile not found for user: " + email));

        if (!ProfileType.STUDENT.name().equals(profile.getProfileType())) {
            throw new WrongProfileTypeException(
                    "Account type is not STUDENT. Current type: " + profile.getProfileType());
        }

        if (studentProfileRepository.existsById(profile.getId())) {
            throw new StudentProfileAlreadyExistsException(
                    "Student profile already exists for this account.");
        }

        StudentProfile studentProfile = StudentProfile.builder()
                .profileId(profile.getId())
                .gradeLevel(request.getGradeLevel())
                .targetUniversity(request.getTargetUniversity())
                .schoolName(request.getSchoolName())
                .targetCareer(request.getTargetCareer())
                .studyShift(request.getStudyShift())
                .build();

        return new StudentProfileResponse(studentProfileRepository.save(studentProfile));
    }

    // -------------------------------------------------------------------------
    // Skeleton methods (pre-existing, not yet implementing a US)
    // -------------------------------------------------------------------------

    @Override
    public StudentProfileResponse getStudentProfile(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado para el usuario"));
        StudentProfile studentProfile = studentProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de estudiante no encontrado"));
        return new StudentProfileResponse(studentProfile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateStudentProfile(String email, UpdateStudentProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Profile not found for user: " + email));

        StudentProfile studentProfile = studentProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Student profile not found for user: " + email));

        if (request.getSchoolName() != null)        studentProfile.setSchoolName(request.getSchoolName());
        if (request.getGradeLevel() != null)        studentProfile.setGradeLevel(request.getGradeLevel());
        if (request.getTargetUniversity() != null)  studentProfile.setTargetUniversity(request.getTargetUniversity());
        if (request.getTargetCareer() != null)      studentProfile.setTargetCareer(request.getTargetCareer());
        if (request.getStudyShift() != null)        studentProfile.setStudyShift(request.getStudyShift());

        return new StudentProfileResponse(studentProfileRepository.save(studentProfile));
    }

    // -------------------------------------------------------------------------
    // US09 — Update teacher specialty
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public TeacherProfileResponse updateTeacherProfile(String email, UpdateTeacherProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Profile not found for user: " + email));

        TeacherProfile teacherProfile = teacherProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Teacher profile not found for user: " + email));

        teacherProfile.setSpecialty(request.getSpecialty());
        if (request.getInstitutionName() != null) teacherProfile.setInstitutionName(request.getInstitutionName());
        if (request.getBioProfessional()  != null) teacherProfile.setBioProfessional(request.getBioProfessional());

        return new TeacherProfileResponse(teacherProfileRepository.save(teacherProfile));
    }

    // -------------------------------------------------------------------------
    // US08 — Create teacher profile
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public TeacherProfileResponse createTeacherProfile(String email, CreateTeacherProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Profile not found for user: " + email));

        if (!ProfileType.TEACHER.name().equals(profile.getProfileType())) {
            throw new WrongProfileTypeException(
                    "Account type is not TEACHER. Current type: " + profile.getProfileType());
        }

        if (teacherProfileRepository.existsById(profile.getId())) {
            throw new TeacherProfileAlreadyExistsException(
                    "Teacher profile already exists for this account.");
        }

        TeacherProfile teacherProfile = TeacherProfile.builder()
                .profileId(profile.getId())
                .specialty(request.getSpecialty())
                .institutionName(request.getInstitutionName())
                .bioProfessional(request.getBioProfessional())
                .build();

        return new TeacherProfileResponse(teacherProfileRepository.save(teacherProfile));
    }
}

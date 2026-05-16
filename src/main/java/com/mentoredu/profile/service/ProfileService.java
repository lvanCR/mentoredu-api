package com.mentoredu.profile.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.exception.ProfileAlreadyExistsException;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.model.StudentProfile;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.StudentProfileRepository;
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
    public StudentProfileResponse updateStudentProfile(UUID userId, UpdateStudentProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado para el usuario"));
        StudentProfile studentProfile = studentProfileRepository.findById(profile.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de estudiante no encontrado"));
        if (request.getSchoolName() != null)        studentProfile.setSchoolName(request.getSchoolName());
        if (request.getGradeLevel() != null)        studentProfile.setGradeLevel(request.getGradeLevel());
        if (request.getTargetUniversity() != null)  studentProfile.setTargetUniversity(request.getTargetUniversity());
        if (request.getTargetCareer() != null)      studentProfile.setTargetCareer(request.getTargetCareer());
        if (request.getStudyShift() != null)        studentProfile.setStudyShift(request.getStudyShift());
        return new StudentProfileResponse(studentProfileRepository.save(studentProfile));
    }
}

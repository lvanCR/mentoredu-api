package com.mentoredu.profile.service;

import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.model.StudentProfile;
import com.mentoredu.profile.repository.ProfileRepository;
import com.mentoredu.profile.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final ProfileRepository profileRepository;
    private final StudentProfileRepository studentProfileRepository;

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
        if (request.getSchoolName() != null) studentProfile.setSchoolName(request.getSchoolName());
        if (request.getGradeLevel() != null) studentProfile.setGradeLevel(request.getGradeLevel());
        if (request.getTargetUniversity() != null) studentProfile.setTargetUniversity(request.getTargetUniversity());
        if (request.getTargetCareer() != null) studentProfile.setTargetCareer(request.getTargetCareer());
        if (request.getStudyShift() != null) studentProfile.setStudyShift(request.getStudyShift());
        return new StudentProfileResponse(studentProfileRepository.save(studentProfile));
    }
}

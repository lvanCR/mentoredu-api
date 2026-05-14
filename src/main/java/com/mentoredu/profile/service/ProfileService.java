package com.mentoredu.profile.service;

import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.model.StudentProfile;
import com.mentoredu.profile.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final StudentProfileRepository studentProfileRepository;

    @Override
    public StudentProfileResponse getStudentProfile(UUID userId) {
        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de estudiante no encontrado"));
        return new StudentProfileResponse(profile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateStudentProfile(UUID userId, UpdateStudentProfileRequest request) {
        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de estudiante no encontrado"));
        if (request.getSchoolName() != null) profile.setSchoolName(request.getSchoolName());
        if (request.getGradeLevel() != null) profile.setGradeLevel(request.getGradeLevel());
        if (request.getTargetUniversity() != null) profile.setTargetUniversity(request.getTargetUniversity());
        if (request.getTargetCareer() != null) profile.setTargetCareer(request.getTargetCareer());
        if (request.getStudyShift() != null) profile.setStudyShift(request.getStudyShift());
        if (request.getIsAnonymousAllowed() != null) profile.setIsAnonymousAllowed(request.getIsAnonymousAllowed());
        return new StudentProfileResponse(studentProfileRepository.save(profile));
    }
}

package com.mentoredu.profile.service;

import com.mentoredu.profile.dto.*;

import java.util.UUID;

public interface IProfileService {
    ProfileResponse selectAccountType(String email, SelectAccountTypeRequest request);
    ProfileResponse updateProfile(String email, UpdateProfileRequest request);
    ProfileMeResponse getMyProfile(String email);
    ProfileResponse getPublicProfile(UUID userId);

    StudentProfileResponse createStudentProfile(String email, CreateStudentProfileRequest request);
    StudentProfileResponse getStudentProfile(UUID userId);
    StudentProfileResponse updateStudentProfile(String email, UpdateStudentProfileRequest request);

    TeacherProfileResponse createTeacherProfile(String email, CreateTeacherProfileRequest request);
    TeacherProfileResponse updateTeacherProfile(String email, UpdateTeacherProfileRequest request);

    AcademyProfileResponse createAcademyProfile(String email, CreateAcademyProfileRequest request);
    AcademyProfileResponse updateAcademyProfile(String email, UpdateAcademyProfileRequest request);
}

package com.mentoredu.profile.service;

import com.mentoredu.profile.dto.CreateStudentProfileRequest;
import com.mentoredu.profile.dto.ProfileMeResponse;
import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateProfileRequest;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;

import java.util.UUID;

public interface IProfileService {
    ProfileResponse selectAccountType(String email, SelectAccountTypeRequest request);
    ProfileResponse updateProfile(String email, UpdateProfileRequest request);
    ProfileMeResponse getMyProfile(String email);
    StudentProfileResponse createStudentProfile(String email, CreateStudentProfileRequest request);
    StudentProfileResponse getStudentProfile(UUID userId);
    StudentProfileResponse updateStudentProfile(String email, UpdateStudentProfileRequest request);
}

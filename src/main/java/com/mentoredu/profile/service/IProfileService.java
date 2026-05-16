package com.mentoredu.profile.service;

import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;

import java.util.UUID;

public interface IProfileService {
    ProfileResponse selectAccountType(String email, SelectAccountTypeRequest request);
    StudentProfileResponse getStudentProfile(UUID userId);
    StudentProfileResponse updateStudentProfile(UUID userId, UpdateStudentProfileRequest request);
}

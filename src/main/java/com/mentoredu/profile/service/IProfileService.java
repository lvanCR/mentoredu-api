package com.mentoredu.profile.service;

import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;

import java.util.UUID;

public interface IProfileService {
    StudentProfileResponse getStudentProfile(UUID userId);
    StudentProfileResponse updateStudentProfile(UUID userId, UpdateStudentProfileRequest request);
}

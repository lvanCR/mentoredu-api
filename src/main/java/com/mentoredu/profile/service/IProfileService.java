package com.mentoredu.profile.service;

import com.mentoredu.profile.dto.CreateOrganizationProfileRequest;
import com.mentoredu.profile.dto.CreateStudentProfileRequest;
import com.mentoredu.profile.dto.CreateTeacherProfileRequest;
import com.mentoredu.profile.dto.OrganizationProfileResponse;
import com.mentoredu.profile.dto.ProfileResponse;
import com.mentoredu.profile.dto.SelectAccountTypeRequest;
import com.mentoredu.profile.dto.StudentProfileResponse;
import com.mentoredu.profile.dto.TeacherProfileResponse;
import com.mentoredu.profile.dto.UpdateProfileRequest;
import com.mentoredu.profile.dto.UpdateStudentProfileRequest;
import com.mentoredu.profile.dto.UpdateTeacherProfileRequest;

import java.util.UUID;

public interface IProfileService {
    ProfileResponse selectAccountType(String email, SelectAccountTypeRequest request);
    ProfileResponse updateProfile(String email, UpdateProfileRequest request);
    StudentProfileResponse createStudentProfile(String email, CreateStudentProfileRequest request);
    StudentProfileResponse getStudentProfile(UUID userId);
    StudentProfileResponse updateStudentProfile(String email, UpdateStudentProfileRequest request);
    TeacherProfileResponse createTeacherProfile(String email, CreateTeacherProfileRequest request);
    TeacherProfileResponse updateTeacherProfile(String email, UpdateTeacherProfileRequest request);
    OrganizationProfileResponse createOrganizationProfile(String email, CreateOrganizationProfileRequest request);
}

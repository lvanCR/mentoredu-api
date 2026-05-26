package com.mentoredu.community.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;
import com.mentoredu.community.model.VerificationStatus;

import java.util.UUID;

public interface IVerificationService {
    VerificationResponse submit(CreateVerificationRequest request, String userEmail);
    PagedResponse<VerificationResponse> getMyRequests(String userEmail, int page, int size);
    PagedResponse<VerificationResponse> getAllRequests(VerificationStatus status, int page, int size);
    VerificationResponse review(UUID requestId, ReviewVerificationRequest request, String reviewerEmail);
}

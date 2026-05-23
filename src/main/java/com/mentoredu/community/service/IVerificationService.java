package com.mentoredu.community.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;

import java.util.List;
import java.util.UUID;

public interface IVerificationService {
    VerificationResponse submit(CreateVerificationRequest request, String userEmail);
    List<VerificationResponse> getMyRequests(String userEmail);
    PagedResponse<VerificationResponse> getAllRequests(int page, int size);
    VerificationResponse review(UUID requestId, ReviewVerificationRequest request, String reviewerEmail);
}

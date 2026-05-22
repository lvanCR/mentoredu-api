package com.mentoredu.verification.service;

import com.mentoredu.verification.dto.SubmitVerificationRequest;
import com.mentoredu.verification.dto.VerificationRequestResponse;

import java.util.List;
import java.util.UUID;

public interface IVerificationService {
    VerificationRequestResponse submit(String userEmail, SubmitVerificationRequest request);
    List<VerificationRequestResponse> getMyRequests(String userEmail);
    VerificationRequestResponse processRequest(UUID requestId, String newStatus, String moderatorEmail);
}

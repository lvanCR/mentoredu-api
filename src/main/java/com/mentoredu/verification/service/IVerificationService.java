package com.mentoredu.verification.service;

import com.mentoredu.verification.dto.SubmitVerificationRequest;
import com.mentoredu.verification.dto.VerificationRequestResponse;

import java.util.List;

public interface IVerificationService {
    VerificationRequestResponse submit(String userEmail, SubmitVerificationRequest request);
    List<VerificationRequestResponse> getMyRequests(String userEmail);
}

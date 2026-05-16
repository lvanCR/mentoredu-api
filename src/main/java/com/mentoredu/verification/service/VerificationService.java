package com.mentoredu.verification.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.repository.UserRepository;
import com.mentoredu.verification.dto.SubmitVerificationRequest;
import com.mentoredu.verification.dto.VerificationRequestResponse;
import com.mentoredu.verification.model.VerificationRequest;
import com.mentoredu.verification.repository.VerificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationService implements IVerificationService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VerificationRequestResponse submit(String userEmail, SubmitVerificationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        VerificationRequest verificationRequest = VerificationRequest.builder()
                .user(user)
                .entityType(request.getEntityType())
                .status("PENDING")
                .build();
        return new VerificationRequestResponse(verificationRequestRepository.save(verificationRequest));
    }

    @Override
    public List<VerificationRequestResponse> getMyRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return verificationRequestRepository.findByUserId(user.getId()).stream()
                .map(VerificationRequestResponse::new)
                .toList();
    }
}

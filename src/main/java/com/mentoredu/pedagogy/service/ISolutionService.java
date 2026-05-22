package com.mentoredu.pedagogy.service;

import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;

import java.util.List;
import java.util.UUID;

public interface ISolutionService {
    SolutionResponse submit(UUID resourceId, SubmitSolutionRequest request, String studentEmail);
    SolutionResponse getMine(UUID resourceId, String studentEmail);
    List<SolutionResponse> listByResource(UUID resourceId, String requesterEmail);
}

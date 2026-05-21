package com.mentoredu.library.service;

import com.mentoredu.library.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.library.dto.SolutionDetailResponse;
import com.mentoredu.library.dto.SolutionResponse;
import com.mentoredu.library.dto.SubmitSolutionRequest;

import java.util.List;
import java.util.UUID;

public interface IResourceSolutionService {

    SolutionResponse submitSolution(UUID resourceId, SubmitSolutionRequest request, String studentEmail);

    List<SolutionDetailResponse> getSolutionsForResource(UUID resourceId, String requesterEmail);

    SolutionDetailResponse getSolutionById(UUID resourceId, UUID solutionId, String requesterEmail);

    MySolutionWithFeedbackResponse getMySubmittedSolution(UUID resourceId, String studentEmail);
}

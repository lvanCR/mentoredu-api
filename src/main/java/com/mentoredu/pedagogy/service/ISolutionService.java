package com.mentoredu.pedagogy.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.pedagogy.dto.MySolutionSummaryResponse;
import com.mentoredu.pedagogy.dto.MySolutionWithFeedbackResponse;
import com.mentoredu.pedagogy.dto.ReceivedSolutionResponse;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.dto.SubmitSolutionRequest;

import java.util.List;
import java.util.UUID;

public interface ISolutionService {
    SolutionResponse submit(UUID resourceId, SubmitSolutionRequest request, String studentEmail);
    MySolutionWithFeedbackResponse getMyWithFeedback(UUID resourceId, String studentEmail);
    List<SolutionResponse> listByResource(UUID resourceId, String requesterEmail);
    SolutionResponse getById(UUID resourceId, UUID solutionId, String requesterEmail);
    SolutionResponse update(UUID resourceId, UUID solutionId, SubmitSolutionRequest request, String requesterEmail);
    void delete(UUID resourceId, UUID solutionId, String requesterEmail);
    PagedResponse<MySolutionSummaryResponse> getMySolutions(String studentEmail, int page, int size);
    PagedResponse<ReceivedSolutionResponse> getReceivedSolutions(String teacherEmail, int page, int size);
}

package com.mentoredu.library.service;

import com.mentoredu.library.dto.SolutionResponse;
import com.mentoredu.library.dto.SubmitSolutionRequest;

import java.util.UUID;

public interface IResourceSolutionService {

    SolutionResponse submitSolution(UUID resourceId, SubmitSolutionRequest request, String studentEmail);
}

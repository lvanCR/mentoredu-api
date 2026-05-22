package com.mentoredu.pedagogy.service;

import com.mentoredu.pedagogy.dto.CreateFeedbackRequest;
import com.mentoredu.pedagogy.dto.FeedbackResponse;

import java.util.UUID;

public interface IFeedbackService {
    FeedbackResponse create(UUID solutionId, CreateFeedbackRequest request, String authorEmail);
    FeedbackResponse getBySolution(UUID solutionId);
}

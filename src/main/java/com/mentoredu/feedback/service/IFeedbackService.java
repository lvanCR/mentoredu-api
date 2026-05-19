package com.mentoredu.feedback.service;

import com.mentoredu.feedback.dto.FeedbackRequest;
import com.mentoredu.feedback.dto.FeedbackResponse;

import java.util.List;

public interface IFeedbackService {
    FeedbackResponse provideFeedback(String authorEmail, FeedbackRequest request);
    List<FeedbackResponse> getReceivedFeedback(String targetEmail);
}

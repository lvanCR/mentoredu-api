package com.mentoredu.pedagogy.dto;

import com.mentoredu.pedagogy.model.FeedbackEntry;
import com.mentoredu.pedagogy.model.Solution;

public record MySolutionWithFeedbackResponse(
        SolutionResponse solution,
        FeedbackResponse feedback
) {
    public static MySolutionWithFeedbackResponse of(Solution solution, FeedbackEntry feedbackEntry) {
        return new MySolutionWithFeedbackResponse(
                SolutionResponse.from(solution),
                feedbackEntry != null ? FeedbackResponse.from(feedbackEntry) : null
        );
    }
}

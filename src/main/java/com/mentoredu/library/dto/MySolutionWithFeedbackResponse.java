package com.mentoredu.library.dto;

import com.mentoredu.feedback.model.FeedbackEntry;
import com.mentoredu.library.model.AcademicResource;
import com.mentoredu.library.model.ResourceFile;
import com.mentoredu.library.model.ResourceSolution;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MySolutionWithFeedbackResponse {

    private final UUID solutionId;
    private final UUID resourceId;
    private final String resourceTitle;
    private final UUID fileId;
    private final String fileUrl;
    private final String status;
    private final LocalDateTime submittedAt;
    private final FeedbackSummary feedback;

    public MySolutionWithFeedbackResponse(ResourceSolution solution, AcademicResource resource,
                                          ResourceFile file, FeedbackEntry feedbackEntry) {
        this.solutionId = solution.getId();
        this.resourceId = solution.getResourceId();
        this.resourceTitle = resource.getTitle();
        this.fileId = solution.getFileId();
        this.fileUrl = file != null ? file.getFileUrl() : null;
        this.status = solution.getStatus();
        this.submittedAt = solution.getSubmittedAt();
        this.feedback = feedbackEntry != null ? new FeedbackSummary(feedbackEntry) : null;
    }

    @Getter
    public static class FeedbackSummary {
        private final UUID id;
        private final String body;
        private final BigDecimal score;
        private final String authorName;
        private final LocalDateTime createdAt;

        public FeedbackSummary(FeedbackEntry entry) {
            this.id = entry.getId();
            this.body = entry.getBody();
            this.score = entry.getScore();
            this.authorName = entry.getAuthor().getFirstName() + " " + entry.getAuthor().getLastName();
            this.createdAt = entry.getCreatedAt();
        }
    }
}

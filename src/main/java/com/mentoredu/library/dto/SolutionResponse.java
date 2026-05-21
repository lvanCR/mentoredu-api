package com.mentoredu.library.dto;

import com.mentoredu.library.model.ResourceSolution;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SolutionResponse {

    private final UUID solutionId;
    private final UUID resourceId;
    private final UUID studentId;
    private final UUID fileId;
    private final String status;
    private final LocalDateTime submittedAt;

    public SolutionResponse(ResourceSolution solution) {
        this.solutionId = solution.getId();
        this.resourceId = solution.getResourceId();
        this.studentId = solution.getStudentId();
        this.fileId = solution.getFileId();
        this.status = solution.getStatus();
        this.submittedAt = solution.getSubmittedAt();
    }
}

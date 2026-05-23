package com.mentoredu.pedagogy.event;

import java.util.UUID;

public record SolutionSubmittedEvent(
        UUID solutionId,
        UUID resourceId,
        UUID exerciseAuthorId,
        UUID studentId
) {}

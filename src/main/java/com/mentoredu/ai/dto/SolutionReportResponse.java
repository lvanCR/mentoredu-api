package com.mentoredu.ai.dto;

import com.mentoredu.pedagogy.dto.SolutionResponse;
import java.util.List;

public record SolutionReportResponse(List<SolutionResponse> solutions, SolutionInsight insight) {}

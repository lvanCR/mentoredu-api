package com.mentoredu.moderation.service;

import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;
import com.mentoredu.moderation.dto.ResolveReportRequest;
import com.mentoredu.moderation.dto.ResolveReportResponse;

import java.util.UUID;

public interface IReportService {
    ReportResponse create(ReportRequest request, String reporterEmail);
    ResolveReportResponse resolve(UUID reportId, ResolveReportRequest request, String moderatorEmail);
}

package com.mentoredu.moderation.service;

import com.mentoredu.moderation.dto.ReportRequest;
import com.mentoredu.moderation.dto.ReportResponse;

public interface IReportService {
    ReportResponse create(ReportRequest request, String reporterEmail);
}

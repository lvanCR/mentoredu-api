package com.mentoredu.report.service;

import com.mentoredu.report.dto.ReportRequest;
import com.mentoredu.report.dto.ReportResponse;

public interface IReportService {
    ReportResponse create(ReportRequest request, String reporterEmail);
}

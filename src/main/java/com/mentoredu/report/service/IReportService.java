package com.mentoredu.report.service;

import com.mentoredu.report.dto.ReportRequest;
import com.mentoredu.report.model.Report;

public interface IReportService {
    Report create(ReportRequest request, String reporterEmail);
}

package com.mentoredu.community.service;

import com.mentoredu.config.PagedResponse;
import com.mentoredu.community.dto.ReportRequest;
import com.mentoredu.community.dto.ReportResponse;
import com.mentoredu.community.dto.ResolveReportRequest;

import java.util.UUID;

public interface IReportService {
    ReportResponse create(ReportRequest request, String reporterEmail);
    PagedResponse<ReportResponse> listOpen(int page, int size);
    ReportResponse resolve(UUID reportId, ResolveReportRequest request, String resolverEmail);
}

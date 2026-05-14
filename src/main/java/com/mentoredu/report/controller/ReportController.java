package com.mentoredu.report.controller;

import com.mentoredu.report.dto.ReportRequest;
import com.mentoredu.report.dto.ReportResponse;
import com.mentoredu.report.service.IReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/moderation/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest request,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.create(request, userDetails.getUsername()));
    }
}

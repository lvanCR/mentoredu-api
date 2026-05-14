package com.mentoredu.content.dto;

public record DownloadDocumentResponse(
        String message,
        DocumentResponse document,
        Integer remainingDailyDownloads,
        boolean dailyLimitApplied,
        boolean paidWithCoins
) {}

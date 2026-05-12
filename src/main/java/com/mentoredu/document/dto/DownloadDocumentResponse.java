package com.mentoredu.document.dto;

public record DownloadDocumentResponse(
        String message,
        DocumentResponse document,
        Integer remainingDailyDownloads,
        boolean dailyLimitApplied,
        boolean paidWithCoins
) {
}

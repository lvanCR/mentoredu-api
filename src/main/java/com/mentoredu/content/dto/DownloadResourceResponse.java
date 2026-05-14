package com.mentoredu.content.dto;

public record DownloadResourceResponse(
        String message,
        ResourceResponse resource,
        Integer remainingDailyDownloads,
        boolean dailyLimitApplied,
        boolean paidWithCoins
) {}

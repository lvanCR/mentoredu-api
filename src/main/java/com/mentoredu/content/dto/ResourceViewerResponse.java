package com.mentoredu.content.dto;

import java.util.UUID;

public record ResourceViewerResponse(
        UUID resourceId,
        String title,
        String previewUrl,
        String downloadUrl,
        String lowResolutionPreviewUrl,
        boolean mobileFirst,
        boolean supportsScroll,
        boolean supportsZoom,
        boolean supportsRotation,
        boolean lazyLoading,
        String loadingStrategy
) {}

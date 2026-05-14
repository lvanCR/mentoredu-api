package com.mentoredu.content.dto;

import java.util.UUID;

public record DocumentViewerResponse(
        UUID documentId,
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

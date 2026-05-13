package com.mentoredu.document.dto;

public record DocumentViewerResponse(
        java.util.UUID documentId,
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
) {
}

package com.mentoredu.document.dto;

public record DocumentViewerResponse(
        Long documentId,
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

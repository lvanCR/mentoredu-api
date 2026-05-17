package com.mentoredu.library.dto;

import org.springframework.core.io.Resource;

public record DownloadResult(Resource resource, String fileName) {}

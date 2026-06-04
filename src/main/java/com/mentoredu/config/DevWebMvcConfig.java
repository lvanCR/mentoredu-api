package com.mentoredu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@Profile("!prod")
public class DevWebMvcConfig implements WebMvcConfigurer {

    @Value("${app.image.upload-dir:uploads/images}")
    private String imageUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(imageUploadDir).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(location);
    }
}

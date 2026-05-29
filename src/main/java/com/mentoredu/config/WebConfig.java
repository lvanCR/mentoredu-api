package com.mentoredu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Expone el directorio físico uploads/ como recursos estáticos bajo /uploads/**.
     *
     * El servicio ResourceFileService guarda los archivos en uploads/resources/{uuid}.pdf
     * (relativo al directorio de trabajo del proceso, es decir, la raíz del proyecto).
     *
     * Mapeo resultante:
     *   GET /uploads/resources/{uuid}.pdf  →  {project-root}/uploads/resources/{uuid}.pdf
     *
     * "file:uploads/" es una ruta relativa que Spring resuelve contra el working directory
     * del proceso, compatible con Windows y Linux sin hardcodear rutas absolutas.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

package com.mentoredu.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT de acceso obtenido en POST /auth/login (expira en 15 min)"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI mentoreduOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MentorEdu API")
                        .description("""
                                API REST de MentorEdu v2.0 — plataforma educativa colaborativa.

                                **Bounded Contexts:** Auth · Profile · Library · Forum · Pedagogy · Community · Catalog

                                **Autenticación:** Bearer JWT. Obtener token en `POST /api/v1/auth/login` \
                                y enviarlo en el header `Authorization: Bearer <token>`.

                                **Refresh:** el access token expira en 15 min; \
                                usar `POST /api/v1/auth/refresh` con el refresh token (7 días).
                                """)
                        .version("v2.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://mentoredu-api.onrender.com").description("Producción (Render)")
                ));
    }
}

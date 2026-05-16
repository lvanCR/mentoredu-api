package com.mentoredu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mentoreduOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MentorEdu API")
                        .description("API REST de la plataforma educativa MentorEdu — colaborativa, gamificada y mobile.")
                        .version("v1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")
                ));
    }
}

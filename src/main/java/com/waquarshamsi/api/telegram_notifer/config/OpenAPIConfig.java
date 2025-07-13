package com.waquarshamsi.api.telegram_notifer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .version("1.0.0")
                        .description("""
                                A robust microservice for dispatching notifications through various channels.
                                This API provides endpoints for managing the Dead Letter Queue (DLQ).
                                """).license(new License()
                                .name("Creative Commons Attribution-NonCommercial 4.0 International")
                                .url("https://creativecommons.org/licenses/by-nc/4.0/")));
    }
}
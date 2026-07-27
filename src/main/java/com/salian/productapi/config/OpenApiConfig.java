package com.salian.productapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productApiOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Product API")
                .description("Spring Boot 3 REST API with JPA, PostgreSQL and OpenAPI")
                .version("v1")
                .contact(new Contact().name("Dharmender Salian"))
                .license(new License().name("MIT")));
    }
}

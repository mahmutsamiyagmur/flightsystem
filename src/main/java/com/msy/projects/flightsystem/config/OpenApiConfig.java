package com.msy.projects.flightsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flightSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight System API")
                        .description("REST API for a flight route calculation system")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MSY Projects")
                                .email("info@msyprojects.com")));
    }
}

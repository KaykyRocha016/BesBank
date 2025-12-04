package org.example.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI besBankOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("BesBank Team");

        Info info = new Info()
                .title("BesBank API - CQRS & Event Sourcing")
                .version("1.0.0")
                .contact(contact)
                .description("API bancária implementada com CQRS, Event Sourcing e Kafka");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}

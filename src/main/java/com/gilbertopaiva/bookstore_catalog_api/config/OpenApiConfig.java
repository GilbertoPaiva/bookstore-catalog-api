package com.gilbertopaiva.bookstore_catalog_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookstore Catalog API")
                        .description("""
                                REST API para catálogo de livros com paginação, filtros avançados e cache.
                                
                                **Funcionalidades:**
                                - CRUD completo de livros e categorias
                                - Paginação e ordenação (`?page=0&size=10&sort=title,asc`)
                                - Filtros: título (like), categoria, faixa de preço
                                - Cache com Caffeine (TTL 10 min) — endpoint `/books/cache/stats`
                                - Resolução de N+1 com `@EntityGraph`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gilberto Paiva")
                                .url("https://github.com/gilbertopaiva")
                                .email("gilberto@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("/").description("Current server")
                ));
    }
}


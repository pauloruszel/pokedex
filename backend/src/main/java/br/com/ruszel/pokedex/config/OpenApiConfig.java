package br.com.ruszel.pokedex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    public static final String ADMIN_TOKEN_SCHEME = "AdminToken";

    @Bean
    public OpenAPI pokedexOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pokédex Clean Architecture API")
                        .version("1.0.0")
                        .description("""
                                API da Pokédex para consulta de Pokémon, cache de imagens, tradução pt-BR e cartas do modo Trunfo.

                                O frontend consome somente esta API. Dados externos vêm da PokeAPI, mas são normalizados, cacheados e servidos pelo backend.

                                Endpoints administrativos exigem o header `X-Admin-Token`.
                                """)
                        .contact(new Contact()
                                .name("Projeto Pokédex Clean")
                                .url("https://github.com/pauloruszel/pokedex"))
                        .license(new License()
                                .name("Uso educacional")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Backend local"),
                        new Server().url("http://localhost:3000").description("Frontend local via Docker")
                ))
                .components(new Components()
                        .addSecuritySchemes(ADMIN_TOKEN_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Admin-Token")
                                .description("Token administrativo configurado em POKEDEX_ADMIN_TOKEN.")));
    }
}

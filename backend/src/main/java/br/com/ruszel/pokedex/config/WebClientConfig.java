package br.com.ruszel.pokedex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private static final int MAX_IN_MEMORY_SIZE = 8 * 1024 * 1024;

    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE));
    }

    @Bean
    WebClient pokeApiWebClient(WebClient.Builder webClientBuilder, @Value("${pokedex.pokeapi.base-url}") String baseUrl) {
        return webClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
    }
}

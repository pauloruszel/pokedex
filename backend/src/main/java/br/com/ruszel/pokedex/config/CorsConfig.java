package br.com.ruszel.pokedex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    private final List<String> allowedOrigins;
    private final List<String> allowedOriginPatterns;

    public CorsConfig(
            @Value("${pokedex.cors.allowed-origins}") String allowedOrigins,
            @Value("${pokedex.cors.allowed-origin-patterns}") String allowedOriginPatterns
    ) {
        this.allowedOrigins = splitCsv(allowedOrigins);
        this.allowedOriginPatterns = splitCsv(allowedOriginPatterns);
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }

    private static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}

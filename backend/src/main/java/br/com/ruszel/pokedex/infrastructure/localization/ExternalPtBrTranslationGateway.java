package br.com.ruszel.pokedex.infrastructure.localization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalPtBrTranslationGateway implements PtBrTranslationGateway {
    private final WebClient.Builder webClientBuilder;

    @Value("${pokedex.translation.enabled:true}")
    private boolean enabled;

    @Value("${pokedex.translation.url:https://api.mymemory.translated.net/get}")
    private String translationUrl;

    @Value("${pokedex.translation.libretranslate-url:}")
    private String libreTranslateUrl;

    @Value("${pokedex.translation.fallback-url:https://translate.googleapis.com}")
    private String fallbackTranslationUrl;

    @Value("${pokedex.translation.timeout-seconds:8}")
    private long timeoutSeconds;

    @Override
    public Optional<String> translate(String sourceText) {
        return translate(sourceText, "en", "pt");
    }

    @Override
    public Optional<String> translate(String sourceText, String sourceLanguage, String targetLanguage) {
        if (!enabled || sourceText == null || sourceText.isBlank()) {
            return Optional.empty();
        }

        if (sourceLanguage.equalsIgnoreCase(targetLanguage)) {
            return Optional.of(clean(sourceText));
        }

        Optional<String> libreTranslate = translateWithLibreTranslate(sourceText, sourceLanguage, targetLanguage);
        if (libreTranslate.isPresent()) {
            return libreTranslate;
        }

        Optional<String> primary = translateWithMyMemory(sourceText, sourceLanguage, targetLanguage);
        if (primary.isPresent()) {
            return primary;
        }

        return translateWithGooglePublicEndpoint(sourceText, sourceLanguage, targetLanguage);
    }

    private Optional<String> translateWithLibreTranslate(String sourceText, String sourceLanguage, String targetLanguage) {
        if (libreTranslateUrl == null || libreTranslateUrl.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode response = webClientBuilder.clone()
                    .baseUrl(libreTranslateUrl)
                    .build()
                    .post()
                    .uri("/translate")
                    .bodyValue(new LibreTranslateRequest(sourceText, sourceLanguage, normalizeTarget(targetLanguage), "text"))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(timeoutSeconds));

            String translated = response == null ? null : response.path("translatedText").asText(null);
            if (translated == null || translated.isBlank() || translated.equalsIgnoreCase(sourceText)) {
                return Optional.empty();
            }

            return Optional.of(clean(translated));
        } catch (Exception exception) {
            log.warn("translation_provider_failed provider=libretranslate url={} message={}", libreTranslateUrl, exception.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> translateWithMyMemory(String sourceText, String sourceLanguage, String targetLanguage) {
        try {
            JsonNode response = webClientBuilder.clone()
                    .baseUrl(translationUrl)
                    .build()
                    .get()
                    .uri(uri -> uri
                            .queryParam("q", sourceText)
                            .queryParam("langpair", normalizeSource(sourceLanguage) + "|" + normalizeMyMemoryTarget(targetLanguage))
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(timeoutSeconds));

            String translated = response == null
                    ? null
                    : response.path("responseData").path("translatedText").asText(null);

            if (translated == null || translated.isBlank() || translated.equalsIgnoreCase(sourceText)) {
                return Optional.empty();
            }

            return Optional.of(clean(translated));
        } catch (Exception exception) {
            log.warn("translation_provider_failed provider=my-memory url={} message={}", translationUrl, exception.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> translateWithGooglePublicEndpoint(String sourceText, String sourceLanguage, String targetLanguage) {
        try {
            JsonNode response = webClientBuilder.clone()
                    .baseUrl(fallbackTranslationUrl)
                    .build()
                    .get()
                    .uri(uri -> uri
                            .path("/translate_a/single")
                            .queryParam("client", "gtx")
                            .queryParam("sl", normalizeSource(sourceLanguage))
                            .queryParam("tl", normalizeTarget(targetLanguage))
                            .queryParam("dt", "t")
                            .queryParam("q", sourceText)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(timeoutSeconds));

            String translated = response == null || !response.isArray()
                    ? null
                    : response.path(0).path(0).path(0).asText(null);

            if (translated == null || translated.isBlank() || translated.equalsIgnoreCase(sourceText)) {
                return Optional.empty();
            }

            return Optional.of(clean(translated));
        } catch (Exception exception) {
            log.warn("translation_provider_failed provider=google-public url={} message={}", fallbackTranslationUrl, exception.getMessage());
            return Optional.empty();
        }
    }

    private String clean(String text) {
        return text
                .replace("\n", " ")
                .replace("\f", " ")
                .replace("Pokemon", "Pokémon")
                .replace("POKéMON", "Pokémon")
                .replace("POKÉMON", "Pokémon")
                .trim();
    }

    private String normalizeSource(String language) {
        if (language == null || language.isBlank()) {
            return "auto";
        }
        return normalizeTarget(language);
    }

    private String normalizeTarget(String language) {
        if (language == null || language.isBlank()) {
            return "pt";
        }
        String normalized = language.trim().toLowerCase();
        if (normalized.startsWith("pt")) {
            return "pt";
        }
        if (normalized.startsWith("es")) {
            return "es";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }
        return normalized;
    }

    private String normalizeMyMemoryTarget(String language) {
        String normalized = normalizeTarget(language);
        return normalized.equals("pt") ? "pt-BR" : normalized;
    }

    private record LibreTranslateRequest(String q, String source, String target, String format) {
    }
}

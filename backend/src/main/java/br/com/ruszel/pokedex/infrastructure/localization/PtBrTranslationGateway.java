package br.com.ruszel.pokedex.infrastructure.localization;

import java.util.Optional;

public interface PtBrTranslationGateway {
    Optional<String> translate(String sourceText);

    Optional<String> translate(String sourceText, String sourceLanguage, String targetLanguage);
}

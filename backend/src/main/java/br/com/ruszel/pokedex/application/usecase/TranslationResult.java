package br.com.ruszel.pokedex.application.usecase;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado da tradução.")
public record TranslationResult(
        @Schema(description = "Texto traduzido.", example = "Um Pokémon rato elétrico.")
        String text,
        @Schema(description = "Locale final do texto.", example = "pt-BR")
        String locale,
        @Schema(description = "Origem do resultado: cache, external-provider, passthrough ou untranslated.", example = "cache")
        String source
) {
}

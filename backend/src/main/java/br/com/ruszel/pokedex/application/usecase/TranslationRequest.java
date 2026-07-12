package br.com.ruszel.pokedex.application.usecase;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição de tradução sob demanda.")
public record TranslationRequest(
        @Schema(description = "Texto original a traduzir.", example = "An electric mouse Pokémon.")
        String text,
        @Schema(description = "Locale de origem.", example = "en")
        String sourceLocale,
        @Schema(description = "Locale de destino.", example = "pt-BR")
        String targetLocale,
        @Schema(description = "Tipo lógico do texto para cache.", example = "flavor_text")
        String kind,
        @Schema(description = "Tipo da entidade relacionada.", example = "pokemon")
        String entityType,
        @Schema(description = "Identificador da entidade relacionada.", example = "25")
        String entityId
) {
}

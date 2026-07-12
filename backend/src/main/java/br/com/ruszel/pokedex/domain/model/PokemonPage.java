package br.com.ruszel.pokedex.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Página de resultados de Pokémon.")
public record PokemonPage(
        @Schema(description = "Total de Pokémon conhecidos para a consulta.", example = "1025")
        Integer count,
        @Schema(description = "Quantidade solicitada na página.", example = "24")
        Integer limit,
        @Schema(description = "Posição inicial da página.", example = "0")
        Integer offset,
        @Schema(description = "Lista de Pokémon resumidos.")
        List<PokemonSummary> results
) {
}

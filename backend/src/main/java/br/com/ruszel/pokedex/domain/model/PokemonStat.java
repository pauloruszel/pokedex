package br.com.ruszel.pokedex.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status base de um Pokémon.")
public record PokemonStat(
        @Schema(description = "Nome do status.", example = "speed")
        String name,
        @Schema(description = "Valor base do status.", example = "90")
        Integer value
) {
}

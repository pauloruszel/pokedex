package br.com.ruszel.pokedex.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações de espécie, preferencialmente localizadas em pt-BR.")
public record PokemonSpecies(
        @Schema(description = "Gênero ou classificação da espécie.", example = "Pokémon Rato")
        String genus,
        @Schema(description = "Descrição curta da Pokédex.", example = "Quando vários destes Pokémon se juntam, sua energia pode causar tempestades elétricas.")
        String flavorText,
        @Schema(description = "Cor principal da espécie.", example = "yellow")
        String color,
        @Schema(description = "Habitat informado pela PokeAPI, quando disponível.", example = "forest")
        String habitat,
        @Schema(description = "Geração de estreia.", example = "generation-i")
        String generation
) {
}

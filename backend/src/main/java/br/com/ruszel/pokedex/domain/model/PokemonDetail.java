package br.com.ruszel.pokedex.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Detalhe completo de um Pokémon para o drawer e comparação.")
public record PokemonDetail(
        @Schema(description = "ID nacional do Pokémon.", example = "25")
        Integer id,
        @Schema(description = "Nome canônico do Pokémon.", example = "pikachu")
        String name,
        @Schema(description = "URL interna da arte oficial.", example = "/api/pokemon/25/images/official-artwork")
        String imageUrl,
        @Schema(description = "URL interna do sprite padrão.", example = "/api/pokemon/25/images/front-default")
        String spriteUrl,
        @Schema(description = "Altura em decímetros, conforme a PokeAPI.", example = "4")
        Integer height,
        @Schema(description = "Peso em hectogramas, conforme a PokeAPI.", example = "60")
        Integer weight,
        @Schema(description = "Tipos do Pokémon.", example = "[\"electric\"]")
        List<String> types,
        @Schema(description = "Habilidades do Pokémon.", example = "[\"static\", \"lightning-rod\"]")
        List<String> abilities,
        @Schema(description = "Status base.")
        List<PokemonStat> stats,
        @Schema(description = "Dados de espécie localizados.")
        PokemonSpecies species,
        @Schema(description = "Linha evolutiva em ordem.", example = "[\"pichu\", \"pikachu\", \"raichu\"]")
        List<String> evolutionChain
) {
}

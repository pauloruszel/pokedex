package br.com.ruszel.pokedex.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resumo usado em listagens e cards da Pokédex.")
public record PokemonSummary(
        @Schema(description = "ID nacional do Pokémon.", example = "25")
        Integer id,
        @Schema(description = "Nome canônico do Pokémon.", example = "pikachu")
        String name,
        @Schema(description = "URL interna da imagem principal servida pelo backend.", example = "/api/pokemon/25/images/official-artwork")
        String imageUrl,
        @Schema(description = "Tipos do Pokémon.", example = "[\"electric\"]")
        List<String> types
) {
}

package br.com.ruszel.pokedex.domain.model;

import java.util.List;

public record PokemonSummary(
        Integer id,
        String name,
        String imageUrl,
        List<String> types
) {
}

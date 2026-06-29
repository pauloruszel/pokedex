package br.com.ruszel.pokedex.domain.model;

import java.util.List;

public record PokemonPage(
        Integer count,
        Integer limit,
        Integer offset,
        List<PokemonSummary> results
) {
}

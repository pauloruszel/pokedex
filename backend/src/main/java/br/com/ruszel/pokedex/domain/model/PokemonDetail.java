package br.com.ruszel.pokedex.domain.model;

import java.util.List;

public record PokemonDetail(
        Integer id,
        String name,
        String imageUrl,
        String spriteUrl,
        Integer height,
        Integer weight,
        List<String> types,
        List<String> abilities,
        List<PokemonStat> stats,
        PokemonSpecies species,
        List<String> evolutionChain
) {
}

package br.com.ruszel.pokedex.domain.model;

public record PokemonSpecies(
        String genus,
        String flavorText,
        String color,
        String habitat,
        String generation
) {
}

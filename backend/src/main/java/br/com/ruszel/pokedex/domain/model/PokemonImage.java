package br.com.ruszel.pokedex.domain.model;

public record PokemonImage(
        Integer pokemonId,
        String imageType,
        String sourceUrl,
        String localPath,
        String publicUrl,
        String contentType,
        Long sizeBytes
) {
}

package br.com.ruszel.pokedex.application.port;

import br.com.ruszel.pokedex.domain.model.PokemonImage;

import java.util.Optional;

public interface ImageStorageGateway {
    PokemonImage cache(Integer pokemonId, String imageType, String sourceUrl, String publicUrl);
    Optional<PokemonImage> read(Integer pokemonId, String imageType);
}

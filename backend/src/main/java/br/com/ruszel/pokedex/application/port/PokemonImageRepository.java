package br.com.ruszel.pokedex.application.port;

import br.com.ruszel.pokedex.domain.model.PokemonImage;

import java.util.Optional;

public interface PokemonImageRepository {
    Optional<PokemonImage> findByPokemonIdAndType(Integer pokemonId, String imageType);
    void save(PokemonImage pokemonImage);
}

package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.PokemonImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetPokemonImageUseCase {
    private final PokemonImageCacheService pokemonImageCacheService;

    public Optional<PokemonImage> execute(Integer pokemonId, String imageType) {
        return pokemonImageCacheService.findCached(pokemonId, imageType);
    }
}

package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.ImageStorageGateway;
import br.com.ruszel.pokedex.application.port.PokemonImageRepository;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetPokemonImageUseCase {
    private final PokemonImageRepository pokemonImageRepository;
    private final ImageStorageGateway imageStorageGateway;

    public Optional<PokemonImage> execute(Integer pokemonId, String imageType) {
        return pokemonImageRepository.findByPokemonIdAndType(pokemonId, imageType)
                .flatMap(this::ensureCachedFileExists);
    }

    private Optional<PokemonImage> ensureCachedFileExists(PokemonImage image) {
        if (image.localPath() != null && imageStorageGateway.read(image.pokemonId(), image.imageType()).isPresent()) {
            return Optional.of(image);
        }
        if (image.sourceUrl() == null || image.sourceUrl().isBlank()) {
            return Optional.empty();
        }
        PokemonImage cached = imageStorageGateway.cache(image.pokemonId(), image.imageType(), image.sourceUrl(), image.publicUrl());
        pokemonImageRepository.save(cached);
        return Optional.of(cached);
    }
}

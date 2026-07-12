package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.ImageStorageGateway;
import br.com.ruszel.pokedex.application.port.PokemonImageRepository;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PokemonImageCacheService {
    private static final String PUBLIC_URL_TEMPLATE = "/api/pokemon/%d/images/%s";

    private final ImageStorageGateway imageStorageGateway;
    private final PokemonImageRepository pokemonImageRepository;

    public String publicUrl(Integer pokemonId, String imageType) {
        return PUBLIC_URL_TEMPLATE.formatted(pokemonId, imageType);
    }

    public Optional<PokemonImage> findCached(Integer pokemonId, String imageType) {
        return pokemonImageRepository.findByPokemonIdAndType(pokemonId, imageType)
                .flatMap(this::ensureCachedFileExists);
    }

    public Optional<PokemonImage> findMetadata(Integer pokemonId, String imageType) {
        return pokemonImageRepository.findByPokemonIdAndType(pokemonId, imageType);
    }

    public boolean hasCachedFile(PokemonImage image) {
        return image.localPath() != null && !image.localPath().isBlank()
                && imageStorageGateway.read(image.pokemonId(), image.imageType()).isPresent();
    }

    public void savePending(Integer pokemonId, String imageType, String sourceUrl, String publicUrl) {
        pokemonImageRepository.save(new PokemonImage(pokemonId, imageType, sourceUrl, null, publicUrl, null, null));
    }

    public PokemonImage cacheAndPersist(Integer pokemonId, String imageType, String sourceUrl, String publicUrl) {
        PokemonImage image = imageStorageGateway.cache(pokemonId, imageType, sourceUrl, publicUrl);
        pokemonImageRepository.save(image);
        return image;
    }

    private Optional<PokemonImage> ensureCachedFileExists(PokemonImage image) {
        if (hasCachedFile(image)) {
            return Optional.of(image);
        }
        if (image.sourceUrl() == null || image.sourceUrl().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cacheAndPersist(image.pokemonId(), image.imageType(), image.sourceUrl(), image.publicUrl()));
    }
}

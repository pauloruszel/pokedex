package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.ImageStorageGateway;
import br.com.ruszel.pokedex.application.port.PokemonImageRepository;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonImageCacheServiceTest {
    @Test
    void cachesAndPersistsImageWhenMetadataHasOnlySourceUrl() {
        InMemoryImageRepository repository = new InMemoryImageRepository();
        repository.save(new PokemonImage(25, "official-artwork", "https://img/pikachu.png", null, "/api/pokemon/25/images/official-artwork", null, null));
        PokemonImageCacheService service = new PokemonImageCacheService(new FakeImageStorageGateway(), repository);

        Optional<PokemonImage> image = service.findCached(25, "official-artwork");

        assertThat(image).isPresent();
        assertThat(image.get().localPath()).isEqualTo("cached/25/official-artwork.png");
        assertThat(repository.saved.localPath()).isEqualTo("cached/25/official-artwork.png");
    }

    private static class InMemoryImageRepository implements PokemonImageRepository {
        private final Map<String, PokemonImage> images = new HashMap<>();
        private PokemonImage saved;

        @Override
        public Optional<PokemonImage> findByPokemonIdAndType(Integer pokemonId, String imageType) {
            return Optional.ofNullable(images.get(key(pokemonId, imageType)));
        }

        @Override
        public void save(PokemonImage pokemonImage) {
            saved = pokemonImage;
            images.put(key(pokemonImage.pokemonId(), pokemonImage.imageType()), pokemonImage);
        }

        private String key(Integer pokemonId, String imageType) {
            return pokemonId + ":" + imageType;
        }
    }

    private static class FakeImageStorageGateway implements ImageStorageGateway {
        @Override
        public PokemonImage cache(Integer pokemonId, String imageType, String sourceUrl, String publicUrl) {
            return new PokemonImage(pokemonId, imageType, sourceUrl, "cached/" + pokemonId + "/" + imageType + ".png", publicUrl, "image/png", 10L);
        }

        @Override
        public Optional<PokemonImage> read(Integer pokemonId, String imageType) {
            return Optional.empty();
        }
    }
}

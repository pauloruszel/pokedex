package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.ImageStorageGateway;
import br.com.ruszel.pokedex.application.port.PokemonImageRepository;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachePokemonImageUseCase {
    private static final String PUBLIC_URL_TEMPLATE = "/api/pokemon/%d/images/%s";

    private final ImageStorageGateway imageStorageGateway;
    private final PokemonImageRepository pokemonImageRepository;
    private final TaskExecutor pokedexTaskExecutor;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    public String execute(Integer pokemonId, String imageType, String sourceUrl) {
        String publicUrl = PUBLIC_URL_TEMPLATE.formatted(pokemonId, imageType);

        if (sourceUrl == null || sourceUrl.isBlank()) {
            return publicUrl;
        }

        var existing = pokemonImageRepository.findByPokemonIdAndType(pokemonId, imageType);
        if (existing.isPresent() && existing.get().localPath() != null && !existing.get().localPath().isBlank()) {
            log.debug("image cache hit pokemonId={} type={}", pokemonId, imageType);
            return existing.get().publicUrl();
        }

        if (existing.isEmpty()) {
            pokemonImageRepository.save(new PokemonImage(pokemonId, imageType, sourceUrl, null, publicUrl, null, null));
        }

        enqueueCache(pokemonId, imageType, sourceUrl, publicUrl);
        return publicUrl;
    }

    private void enqueueCache(Integer pokemonId, String imageType, String sourceUrl, String publicUrl) {
        String key = pokemonId + ":" + imageType;
        if (!inFlight.add(key)) {
            return;
        }

        try {
            pokedexTaskExecutor.execute(() -> {
                try {
                    cacheAndPersist(pokemonId, imageType, sourceUrl, publicUrl);
                } finally {
                    inFlight.remove(key);
                }
            });
        } catch (TaskRejectedException exception) {
            inFlight.remove(key);
            log.debug("image cache queue full pokemonId={} type={}. Keeping internal URL for lazy retry.", pokemonId, imageType);
        }
    }

    private void cacheAndPersist(Integer pokemonId, String imageType, String sourceUrl, String publicUrl) {
        try {
            PokemonImage image = imageStorageGateway.cache(pokemonId, imageType, sourceUrl, publicUrl);
            pokemonImageRepository.save(image);
            log.debug("image cached pokemonId={} type={} sizeBytes={}", pokemonId, imageType, image.sizeBytes());
        } catch (Exception exception) {
            log.warn("Não foi possível cachear imagem {} do Pokémon {}. A URL pública interna será mantida.", imageType, pokemonId, exception);
            pokemonImageRepository.save(new PokemonImage(pokemonId, imageType, sourceUrl, null, publicUrl, null, null));
        }
    }
}

package br.com.ruszel.pokedex.application.usecase;

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
    private final PokemonImageCacheService pokemonImageCacheService;
    private final TaskExecutor pokedexTaskExecutor;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    public String execute(Integer pokemonId, String imageType, String sourceUrl) {
        String publicUrl = pokemonImageCacheService.publicUrl(pokemonId, imageType);

        if (sourceUrl == null || sourceUrl.isBlank()) {
            return publicUrl;
        }

        var existing = pokemonImageCacheService.findMetadata(pokemonId, imageType);
        if (existing.isPresent() && pokemonImageCacheService.hasCachedFile(existing.get())) {
            log.debug("image cache hit pokemonId={} type={}", pokemonId, imageType);
            return existing.get().publicUrl();
        }

        if (existing.isEmpty()) {
            pokemonImageCacheService.savePending(pokemonId, imageType, sourceUrl, publicUrl);
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
            PokemonImage image = pokemonImageCacheService.cacheAndPersist(pokemonId, imageType, sourceUrl, publicUrl);
            log.debug("image cached pokemonId={} type={} sizeBytes={}", pokemonId, imageType, image.sizeBytes());
        } catch (Exception exception) {
            log.warn("Could not cache image {} for Pokemon {}. Internal public URL will be kept.", imageType, pokemonId, exception);
            pokemonImageCacheService.savePending(pokemonId, imageType, sourceUrl, publicUrl);
        }
    }
}

package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.PokemonCatalogRepository;
import br.com.ruszel.pokedex.application.port.PokemonDetailRepository;
import br.com.ruszel.pokedex.domain.model.PokemonPage;
import br.com.ruszel.pokedex.domain.model.TrunfoCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ListTrunfoCardsUseCase {
    private static final int MAX_LIMIT = 80;
    private static final int DEFAULT_LIMIT = 40;

    private final PokemonCatalogRepository catalogRepository;
    private final PokemonDetailRepository detailRepository;
    private final TrunfoCardFactory trunfoCardFactory;

    public Mono<List<TrunfoCard>> execute(int limit, String mode, String type, int offset) {
        int sanitizedLimit = sanitizeLimit(limit);
        int fetchLimit = "balanced".equalsIgnoreCase(mode)
                ? Math.min(MAX_LIMIT, sanitizedLimit * 2)
                : sanitizedLimit;

        Mono<PokemonPage> page = hasText(type)
                ? catalogRepository.findByType(type.trim().toLowerCase(Locale.ROOT), fetchLimit, Math.max(offset, 0))
                : catalogRepository.findAll(fetchLimit, Math.max(offset, 0));

        return page
                .flatMapMany(pokemonPage -> Flux.fromIterable(pokemonPage.results()))
                .flatMap(summary -> detailRepository.findByNameOrId(String.valueOf(summary.id())), 8)
                .map(trunfoCardFactory::toCard)
                .collectList()
                .map(cards -> trunfoCardFactory.selectCards(cards, sanitizedLimit, mode));
    }

    private int sanitizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

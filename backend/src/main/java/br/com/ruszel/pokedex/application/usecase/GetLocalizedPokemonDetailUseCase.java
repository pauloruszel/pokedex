package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class GetLocalizedPokemonDetailUseCase {
    private final GetPokemonDetailUseCase getPokemonDetailUseCase;
    private final PokemonSpeciesTranslator pokemonSpeciesTranslator;

    public Mono<PokemonDetail> execute(String nameOrId, String locale) {
        String targetLocale = normalizeLocale(locale);
        return getPokemonDetailUseCase.execute(nameOrId)
                .flatMap(detail -> {
                    if (PokemonSpeciesTranslator.SOURCE_LOCALE.equals(targetLocale) || detail.species() == null) {
                        return Mono.just(detail);
                    }

                    return Mono.fromCallable(() -> localize(detail, targetLocale))
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }

    private PokemonDetail localize(PokemonDetail detail, String targetLocale) {
        return new PokemonDetail(
                detail.id(),
                detail.name(),
                detail.imageUrl(),
                detail.spriteUrl(),
                detail.height(),
                detail.weight(),
                detail.types(),
                detail.abilities(),
                detail.stats(),
                pokemonSpeciesTranslator.translate(detail.species(), detail.id(), targetLocale),
                detail.evolutionChain()
        );
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return PokemonSpeciesTranslator.SOURCE_LOCALE;
        }
        String normalized = locale.trim().toLowerCase();
        if (normalized.startsWith("pt")) {
            return PokemonSpeciesTranslator.SOURCE_LOCALE;
        }
        if (normalized.startsWith("es")) {
            return "es";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }
        return PokemonSpeciesTranslator.SOURCE_LOCALE;
    }
}

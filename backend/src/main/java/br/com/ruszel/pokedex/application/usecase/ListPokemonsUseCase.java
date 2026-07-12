package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.PokemonCatalogRepository;
import br.com.ruszel.pokedex.domain.model.PokemonPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ListPokemonsUseCase {
    private final PokemonCatalogRepository repository;

    public Mono<PokemonPage> execute(int limit, int offset) {
        return repository.findAll(sanitizeLimit(limit), Math.max(offset, 0));
    }

    private int sanitizeLimit(int limit) {
        return Math.max(1, Math.min(limit, 60));
    }
}

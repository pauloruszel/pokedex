package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.PokemonCatalogRepository;
import br.com.ruszel.pokedex.domain.model.PokemonPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ListPokemonsByTypeUseCase {
    private final PokemonCatalogRepository repository;

    public Mono<PokemonPage> execute(String typeName, int limit, int offset) {
        return repository.findByType(typeName.toLowerCase(), Math.max(1, Math.min(limit, 60)), Math.max(offset, 0));
    }
}

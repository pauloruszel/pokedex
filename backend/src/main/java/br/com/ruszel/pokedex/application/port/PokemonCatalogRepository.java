package br.com.ruszel.pokedex.application.port;

import br.com.ruszel.pokedex.domain.model.PokemonPage;
import reactor.core.publisher.Mono;

public interface PokemonCatalogRepository {
    Mono<PokemonPage> findAll(int limit, int offset);
    Mono<PokemonPage> findByType(String typeName, int limit, int offset);
}

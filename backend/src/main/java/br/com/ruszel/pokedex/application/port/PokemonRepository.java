package br.com.ruszel.pokedex.application.port;

import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import br.com.ruszel.pokedex.domain.model.PokemonPage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PokemonRepository {
    Mono<PokemonPage> findAll(int limit, int offset);
    Mono<PokemonDetail> findByNameOrId(String nameOrId);
    Flux<String> findTypes();
    Mono<PokemonPage> findByType(String typeName, int limit, int offset);
}

package br.com.ruszel.pokedex.application.port;

import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import reactor.core.publisher.Mono;

public interface PokemonDetailRepository {
    Mono<PokemonDetail> findByNameOrId(String nameOrId);
}

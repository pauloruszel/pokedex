package br.com.ruszel.pokedex.application.port;

import reactor.core.publisher.Flux;

public interface PokemonTypeRepository {
    Flux<String> findTypes();
}

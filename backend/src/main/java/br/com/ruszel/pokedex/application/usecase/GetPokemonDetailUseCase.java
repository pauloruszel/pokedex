package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.PokemonDetailRepository;
import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetPokemonDetailUseCase {
    private final PokemonDetailRepository repository;

    public Mono<PokemonDetail> execute(String nameOrId) {
        return repository.findByNameOrId(nameOrId.trim().toLowerCase());
    }
}

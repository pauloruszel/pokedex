package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.ListPokemonsByTypeUseCase;
import br.com.ruszel.pokedex.application.usecase.ListPokemonsUseCase;
import br.com.ruszel.pokedex.application.usecase.ListTypesUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonPage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(value = "/api/pokemon", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PokemonCatalogController {
    private final ListPokemonsUseCase listPokemonsUseCase;
    private final ListTypesUseCase listTypesUseCase;
    private final ListPokemonsByTypeUseCase listPokemonsByTypeUseCase;

    @GetMapping
    public Mono<PokemonPage> list(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return listPokemonsUseCase.execute(limit, offset);
    }

    @GetMapping("/types")
    public Mono<List<String>> types() {
        return listTypesUseCase.execute().collectList();
    }

    @GetMapping("/type/{typeName}")
    public Mono<PokemonPage> byType(
            @PathVariable String typeName,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return listPokemonsByTypeUseCase.execute(typeName, limit, offset);
    }
}

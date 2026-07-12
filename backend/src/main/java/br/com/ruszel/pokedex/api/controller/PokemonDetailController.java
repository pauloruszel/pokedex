package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.GetLocalizedPokemonDetailUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/pokemon", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PokemonDetailController {
    private final GetLocalizedPokemonDetailUseCase getLocalizedPokemonDetailUseCase;

    @GetMapping("/search")
    public Mono<PokemonDetail> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "pt-BR") String locale
    ) {
        return getLocalizedPokemonDetailUseCase.execute(q, locale);
    }

    @GetMapping("/{nameOrId}")
    public Mono<PokemonDetail> detail(
            @PathVariable String nameOrId,
            @RequestParam(defaultValue = "pt-BR") String locale
    ) {
        return getLocalizedPokemonDetailUseCase.execute(nameOrId, locale);
    }
}

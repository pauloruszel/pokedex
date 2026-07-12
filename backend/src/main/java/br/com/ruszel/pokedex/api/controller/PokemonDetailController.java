package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.GetLocalizedPokemonDetailUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pokémon - Detalhes", description = "Busca por nome/número e detalhe localizado em pt-BR.")
public class PokemonDetailController {
    private final GetLocalizedPokemonDetailUseCase getLocalizedPokemonDetailUseCase;

    @GetMapping("/search")
    @Operation(
            summary = "Busca Pokémon por texto",
            description = "Busca um Pokémon por nome ou número e retorna o detalhe com textos localizados no idioma solicitado."
    )
    @ApiResponse(responseCode = "200", description = "Pokémon encontrado", content = @Content(schema = @Schema(implementation = PokemonDetail.class)))
    public Mono<PokemonDetail> search(
            @Parameter(description = "Nome ou número do Pokémon.", example = "pikachu")
            @RequestParam String q,
            @Parameter(description = "Locale usado para textos de espécie.", example = "pt-BR")
            @RequestParam(defaultValue = "pt-BR") String locale
    ) {
        return getLocalizedPokemonDetailUseCase.execute(q, locale);
    }

    @GetMapping("/{nameOrId}")
    @Operation(
            summary = "Obtém detalhe do Pokémon",
            description = "Retorna dados completos do Pokémon, incluindo status, habilidades, espécie, cadeia evolutiva e URLs internas de imagem."
    )
    @ApiResponse(responseCode = "200", description = "Detalhe retornado com sucesso", content = @Content(schema = @Schema(implementation = PokemonDetail.class)))
    public Mono<PokemonDetail> detail(
            @Parameter(description = "Nome ou número do Pokémon.", example = "25")
            @PathVariable String nameOrId,
            @Parameter(description = "Locale usado para textos de espécie.", example = "pt-BR")
            @RequestParam(defaultValue = "pt-BR") String locale
    ) {
        return getLocalizedPokemonDetailUseCase.execute(nameOrId, locale);
    }
}

package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.ListPokemonsByTypeUseCase;
import br.com.ruszel.pokedex.application.usecase.ListPokemonsUseCase;
import br.com.ruszel.pokedex.application.usecase.ListTypesUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

@RestController
@RequestMapping(value = "/api/pokemon", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Pokémon - Catálogo", description = "Listagem paginada, tipos disponíveis e filtro por tipo.")
public class PokemonCatalogController {
    private final ListPokemonsUseCase listPokemonsUseCase;
    private final ListTypesUseCase listTypesUseCase;
    private final ListPokemonsByTypeUseCase listPokemonsByTypeUseCase;

    @GetMapping
    @Operation(
            summary = "Lista Pokémon paginados",
            description = "Retorna uma página de Pokémon resumidos. Usa cache local quando disponível e consulta a PokeAPI quando necessário."
    )
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso", content = @Content(schema = @Schema(implementation = PokemonPage.class)))
    public Mono<PokemonPage> list(
            @Parameter(description = "Quantidade máxima de itens na página.", example = "24")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Posição inicial da página.", example = "0")
            @RequestParam(defaultValue = "0") int offset
    ) {
        return listPokemonsUseCase.execute(limit, offset);
    }

    @GetMapping("/types")
    @Operation(summary = "Lista tipos de Pokémon", description = "Retorna os tipos conhecidos pelo backend para uso em filtros.")
    @ApiResponse(responseCode = "200", description = "Tipos retornados com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    public Mono<List<String>> types() {
        return listTypesUseCase.execute().collectList();
    }

    @GetMapping("/type/{typeName}")
    @Operation(
            summary = "Lista Pokémon por tipo",
            description = "Retorna uma página de Pokémon filtrados por tipo, por exemplo `fire`, `water` ou `grass`."
    )
    @ApiResponse(responseCode = "200", description = "Página filtrada retornada com sucesso", content = @Content(schema = @Schema(implementation = PokemonPage.class)))
    public Mono<PokemonPage> byType(
            @Parameter(description = "Nome do tipo em inglês, conforme a PokeAPI.", example = "fire")
            @PathVariable String typeName,
            @Parameter(description = "Quantidade máxima de itens na página.", example = "24")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Posição inicial da página.", example = "0")
            @RequestParam(defaultValue = "0") int offset
    ) {
        return listPokemonsByTypeUseCase.execute(typeName, limit, offset);
    }
}

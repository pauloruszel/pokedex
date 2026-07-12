package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.ListTrunfoCardsUseCase;
import br.com.ruszel.pokedex.domain.model.TrunfoCard;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(value = "/api/trunfo", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Trunfo", description = "Cartas e atributos calculados para o modo Super Trunfo.")
public class TrunfoController {
    private final ListTrunfoCardsUseCase listTrunfoCardsUseCase;

    @GetMapping("/cards")
    @Operation(
            summary = "Lista cartas de Trunfo",
            description = "Monta cartas com atributos derivados dos dados de Pokémon. Pode balancear raridade e filtrar por tipo."
    )
    @ApiResponse(responseCode = "200", description = "Cartas retornadas com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrunfoCard.class))))
    public Mono<List<TrunfoCard>> cards(
            @Parameter(description = "Quantidade máxima de cartas.", example = "40")
            @RequestParam(defaultValue = "40") int limit,
            @Parameter(description = "Modo de seleção das cartas.", example = "balanced")
            @RequestParam(defaultValue = "balanced") String mode,
            @Parameter(description = "Filtro opcional por tipo.", example = "electric")
            @RequestParam(required = false) String type,
            @Parameter(description = "Posição inicial para paginação.", example = "0")
            @RequestParam(defaultValue = "0") int offset
    ) {
        return listTrunfoCardsUseCase.execute(limit, mode, type, offset);
    }
}

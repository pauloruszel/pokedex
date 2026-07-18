package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.ListTrunfoCardsUseCase;
import br.com.ruszel.pokedex.application.usecase.TrunfoRoomService;
import br.com.ruszel.pokedex.domain.model.TrunfoCard;
import br.com.ruszel.pokedex.domain.model.TrunfoRoomView;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final TrunfoRoomService trunfoRoomService;

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

    @PostMapping("/rooms")
    @Operation(summary = "Cria uma sala privada de Trunfo")
    public Mono<TrunfoRoomView> createRoom(@RequestBody CreateRoomRequest request) {
        return Mono.just(trunfoRoomService.create(request.nickname(), request.mode(), request.difficulty(), request.type(), request.deckSelection(), request.deckSize()));
    }

    @PostMapping("/rooms/{code}/join")
    @Operation(summary = "Entra em uma sala privada de Trunfo")
    public Mono<TrunfoRoomView> joinRoom(@PathVariable String code, @RequestBody JoinRoomRequest request) {
        return trunfoRoomService.join(code, request.nickname());
    }

    @GetMapping("/rooms/{code}")
    @Operation(summary = "Consulta o estado da sala privada de Trunfo")
    public Mono<TrunfoRoomView> room(@PathVariable String code, @RequestParam String playerToken) {
        return Mono.just(trunfoRoomService.get(code, playerToken));
    }

    @PostMapping("/rooms/{code}/rounds")
    @Operation(summary = "Processa uma rodada da sala privada de Trunfo")
    public Mono<TrunfoRoomView> playRound(@PathVariable String code, @RequestBody PlayRoundRequest request) {
        return Mono.just(trunfoRoomService.playRound(code, request.playerToken(), request.attribute()));
    }

    @PostMapping("/rooms/{code}/deck")
    @Operation(summary = "Confirma deck da sala privada de Trunfo")
    public Mono<TrunfoRoomView> confirmDeck(@PathVariable String code, @RequestBody DeckRequest request) {
        return trunfoRoomService.confirmDeck(code, request.playerToken(), request.cardIds());
    }

    @PostMapping("/rooms/{code}/ready")
    @Operation(summary = "Marca jogador como pronto na sala privada de Trunfo")
    public Mono<TrunfoRoomView> ready(@PathVariable String code, @RequestBody PlayerRoomRequest request) {
        return Mono.just(trunfoRoomService.get(code, request.playerToken()));
    }

    @PostMapping("/rooms/{code}/leave")
    @Operation(summary = "Sai da sala privada de Trunfo")
    public Mono<TrunfoRoomView> leave(@PathVariable String code, @RequestBody PlayerRoomRequest request) {
        return Mono.just(trunfoRoomService.leave(code, request.playerToken()));
    }

    public record CreateRoomRequest(String nickname, String mode, String difficulty, String type, String deckSelection, Integer deckSize) {
    }

    public record JoinRoomRequest(String nickname) {
    }

    public record PlayRoundRequest(String playerToken, String attribute) {
    }

    public record PlayerRoomRequest(String playerToken) {
    }

    public record DeckRequest(String playerToken, List<Integer> cardIds) {
    }
}

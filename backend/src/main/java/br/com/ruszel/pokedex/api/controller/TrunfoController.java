package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.ListTrunfoCardsUseCase;
import br.com.ruszel.pokedex.domain.model.TrunfoCard;
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
public class TrunfoController {
    private final ListTrunfoCardsUseCase listTrunfoCardsUseCase;

    @GetMapping("/cards")
    public Mono<List<TrunfoCard>> cards(
            @RequestParam(defaultValue = "40") int limit,
            @RequestParam(defaultValue = "balanced") String mode,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return listTrunfoCardsUseCase.execute(limit, mode, type, offset);
    }
}

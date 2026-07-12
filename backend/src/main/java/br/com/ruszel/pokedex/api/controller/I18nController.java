package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.TranslationRequest;
import br.com.ruszel.pokedex.application.usecase.TranslationResult;
import br.com.ruszel.pokedex.application.usecase.TranslateTextUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
@Tag(name = "i18n", description = "Tradução sob demanda usada pelo frontend.")
public class I18nController {
    private final TranslateTextUseCase translateTextUseCase;

    @PostMapping("/translate")
    @Operation(
            summary = "Traduz texto sob demanda",
            description = "Traduz um texto entre locales e reutiliza o cache de traduções quando já houver uma tradução válida."
    )
    @ApiResponse(responseCode = "200", description = "Tradução processada", content = @Content(schema = @Schema(implementation = TranslationResult.class)))
    public Mono<TranslationResult> translate(@RequestBody TranslationRequest request) {
        return Mono.fromCallable(() -> translateTextUseCase.execute(request))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

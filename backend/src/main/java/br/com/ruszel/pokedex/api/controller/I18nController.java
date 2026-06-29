package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.TranslateTextUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/i18n")
@RequiredArgsConstructor
public class I18nController {
    private final TranslateTextUseCase translateTextUseCase;

    @PostMapping("/translate")
    public TranslateTextUseCase.TranslationResult translate(@RequestBody TranslateTextUseCase.TranslationRequest request) {
        return translateTextUseCase.execute(request);
    }
}

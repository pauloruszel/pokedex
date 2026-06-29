package br.com.ruszel.pokedex.api.controller;

import br.com.ruszel.pokedex.application.usecase.TranslationMaintenanceService;
import br.com.ruszel.pokedex.application.usecase.TranslateTextUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/translations")
@RequiredArgsConstructor
public class TranslationMaintenanceController {
    private final TranslationMaintenanceService translationMaintenanceService;
    private final TranslateTextUseCase translateTextUseCase;

    @GetMapping("/missing")
    public List<TranslationMaintenanceService.MissingTranslation> missing(
            @RequestParam(defaultValue = "2000") int limit
    ) {
        return translationMaintenanceService.findMissingFlavorTexts(limit);
    }

    @PostMapping("/refresh")
    public TranslationMaintenanceService.RefreshResult refresh(
            @RequestParam(defaultValue = "2000") int limit
    ) {
        return translationMaintenanceService.refreshMissingFlavorTexts(limit);
    }

    @GetMapping("/status")
    public Object status() {
        return translationMaintenanceService.refreshStatus();
    }

    @PostMapping("/cleanup-invalid-cache")
    public TranslateTextUseCase.CleanupResult cleanupInvalidCache() {
        return translateTextUseCase.cleanupInvalidCachedTranslations();
    }
}

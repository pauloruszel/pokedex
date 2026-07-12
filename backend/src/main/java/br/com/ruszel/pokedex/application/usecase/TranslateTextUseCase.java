package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.infrastructure.localization.PtBrTranslationGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TranslateTextUseCase {
    private final TranslationCacheService translationCacheService;
    private final PtBrTranslationGateway translationGateway;
    private final TranslationTextNormalizer normalizer;

    @Autowired
    public TranslateTextUseCase(
            TranslationCacheService translationCacheService,
            PtBrTranslationGateway translationGateway,
            TranslationTextNormalizer normalizer
    ) {
        this.translationCacheService = translationCacheService;
        this.translationGateway = translationGateway;
        this.normalizer = normalizer;
    }

    public TranslateTextUseCase(TranslationCacheService translationCacheService, PtBrTranslationGateway translationGateway) {
        this(translationCacheService, translationGateway, new TranslationTextNormalizer());
    }

    public TranslationResult execute(TranslationRequest request) {
        String sourceText = normalizer.normalizeText(request.text());
        String sourceLocale = normalizer.normalizeLocale(request.sourceLocale(), "pt-BR");
        String targetLocale = normalizer.normalizeLocale(request.targetLocale(), "pt-BR");
        String kind = normalizer.normalizeKind(request.kind());
        String entityType = normalizer.normalizeEntity(request.entityType());
        String entityId = normalizer.normalizeEntity(request.entityId());

        if (sourceText.isBlank() || sourceLocale.equals(targetLocale)) {
            return new TranslationResult(sourceText, targetLocale, "passthrough");
        }

        TranslationCacheService.CachedTranslation cached = translationCacheService
                .find(kind, sourceLocale, targetLocale, sourceText)
                .orElse(null);

        if (cached != null && isUsableCachedTranslation(sourceText, cached)) {
            return new TranslationResult(cached.translatedText(), targetLocale, "cache");
        }

        String translated = translationGateway
                .translate(sourceText, normalizer.providerLanguage(sourceLocale), normalizer.providerLanguage(targetLocale))
                .orElse(sourceText);
        String source = translated.equals(sourceText) ? "fallback-source" : "external-provider";

        if ("fallback-source".equals(source)) {
            return new TranslationResult("", targetLocale, "untranslated");
        }

        translationCacheService.save(kind, sourceLocale, targetLocale, sourceText, translated, source, entityType, entityId);

        return new TranslationResult(translated, targetLocale, source);
    }

    private boolean isUsableCachedTranslation(String sourceText, TranslationCacheService.CachedTranslation cached) {
        if (cached.translatedText() == null || cached.translatedText().isBlank()) {
            return false;
        }
        if ("fallback-source".equals(cached.translationSource())) {
            return false;
        }
        return !normalizer.normalizeText(cached.translatedText()).equalsIgnoreCase(sourceText);
    }
}

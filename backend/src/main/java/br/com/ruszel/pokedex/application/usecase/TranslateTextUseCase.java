package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.infrastructure.localization.PtBrTranslationGateway;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Requisição de tradução sob demanda.")
    public record TranslationRequest(
            @Schema(description = "Texto original a traduzir.", example = "An electric mouse Pokémon.")
            String text,
            @Schema(description = "Locale de origem.", example = "en")
            String sourceLocale,
            @Schema(description = "Locale de destino.", example = "pt-BR")
            String targetLocale,
            @Schema(description = "Tipo lógico do texto para cache.", example = "flavor_text")
            String kind,
            @Schema(description = "Tipo da entidade relacionada.", example = "pokemon")
            String entityType,
            @Schema(description = "Identificador da entidade relacionada.", example = "25")
            String entityId
    ) {
    }

    @Schema(description = "Resultado da tradução.")
    public record TranslationResult(
            @Schema(description = "Texto traduzido.", example = "Um Pokémon rato elétrico.")
            String text,
            @Schema(description = "Locale final do texto.", example = "pt-BR")
            String locale,
            @Schema(description = "Origem do resultado: cache, external-provider, passthrough ou untranslated.", example = "cache")
            String source
    ) {
    }

}

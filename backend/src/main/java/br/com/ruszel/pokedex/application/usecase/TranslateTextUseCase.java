package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.infrastructure.localization.PtBrTranslationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TranslateTextUseCase {
    private final JdbcClient jdbcClient;
    private final PtBrTranslationGateway translationGateway;

    public TranslationResult execute(TranslationRequest request) {
        String sourceText = normalizeText(request.text());
        String sourceLocale = normalizeLocale(request.sourceLocale(), "pt-BR");
        String targetLocale = normalizeLocale(request.targetLocale(), "pt-BR");
        String kind = normalizeKind(request.kind());

        if (sourceText.isBlank() || sourceLocale.equals(targetLocale)) {
            return new TranslationResult(sourceText, targetLocale, "passthrough");
        }

        String key = key(kind, sourceLocale, targetLocale, sourceText);
        CachedTranslation cached = jdbcClient.sql("""
                        SELECT translated_text, translation_source
                          FROM pokemon_text_translation
                         WHERE translation_key = :key
                        """)
                .param("key", key)
                .query((rs, rowNum) -> new CachedTranslation(rs.getString("translated_text"), rs.getString("translation_source")))
                .optional()
                .orElse(null);

        if (cached != null && isUsableCachedTranslation(sourceText, cached)) {
            return new TranslationResult(cached.translatedText(), targetLocale, "cache");
        }

        String translated = translationGateway
                .translate(sourceText, toProviderLanguage(sourceLocale), toProviderLanguage(targetLocale))
                .orElse(sourceText);
        String source = translated.equals(sourceText) ? "fallback-source" : "external-provider";

        if ("fallback-source".equals(source)) {
            return new TranslationResult("", targetLocale, "untranslated");
        }

        jdbcClient.sql("""
                        MERGE INTO pokemon_text_translation (
                            translation_key, source_text, text_kind, locale, translated_text, translation_source, updated_at
                        )
                        KEY(translation_key)
                        VALUES (:key, :sourceText, :kind, :locale, :translatedText, :translationSource, CURRENT_TIMESTAMP)
                        """)
                .param("key", key)
                .param("sourceText", sourceText)
                .param("kind", kind)
                .param("locale", targetLocale)
                .param("translatedText", translated)
                .param("translationSource", source)
                .update();

        return new TranslationResult(translated, targetLocale, source);
    }

    public CleanupResult cleanupInvalidCachedTranslations() {
        int deleted = jdbcClient.sql("""
                        DELETE FROM pokemon_text_translation
                         WHERE locale IN ('es', 'en')
                           AND (
                                translation_source = 'fallback-source'
                                OR TRIM(translated_text) = TRIM(source_text)
                           )
                        """)
                .update();

        return new CleanupResult(deleted);
    }

    private boolean isUsableCachedTranslation(String sourceText, CachedTranslation cached) {
        if (cached.translatedText() == null || cached.translatedText().isBlank()) {
            return false;
        }
        if ("fallback-source".equals(cached.translationSource())) {
            return false;
        }
        return !normalizeText(cached.translatedText()).equalsIgnoreCase(sourceText);
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.replace("\n", " ").replace("\f", " ").replace("  ", " ").trim();
    }

    private String normalizeLocale(String locale, String fallback) {
        if (locale == null || locale.isBlank()) {
            return fallback;
        }
        String normalized = locale.trim().toLowerCase();
        if (normalized.startsWith("pt")) {
            return "pt-BR";
        }
        if (normalized.startsWith("es")) {
            return "es";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }
        throw new IllegalArgumentException("Unsupported locale: " + locale);
    }

    private String toProviderLanguage(String locale) {
        return locale.equals("pt-BR") ? "pt" : locale;
    }

    private String normalizeKind(String kind) {
        if (kind == null || kind.isBlank()) {
            return "ui_text";
        }
        return kind.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
    }

    private String key(String kind, String sourceLocale, String targetLocale, String sourceText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((kind + "|" + sourceLocale + "|" + targetLocale + "|" + sourceText).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for translation cache keys", exception);
        }
    }

    public record TranslationRequest(String text, String sourceLocale, String targetLocale, String kind) {
    }

    public record TranslationResult(String text, String locale, String source) {
    }

    public record CleanupResult(int deleted) {
    }

    private record CachedTranslation(String translatedText, String translationSource) {
    }
}

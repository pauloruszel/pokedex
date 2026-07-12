package br.com.ruszel.pokedex.application.usecase;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TranslationCacheService {
    private final JdbcClient jdbcClient;
    private final TranslationCacheKeyFactory keyFactory;

    @Autowired
    public TranslationCacheService(JdbcClient jdbcClient, TranslationCacheKeyFactory keyFactory) {
        this.jdbcClient = jdbcClient;
        this.keyFactory = keyFactory;
    }

    public TranslationCacheService(JdbcClient jdbcClient) {
        this(jdbcClient, new TranslationCacheKeyFactory());
    }

    public Optional<CachedTranslation> find(String kind, String sourceLocale, String targetLocale, String sourceText) {
        return jdbcClient.sql("""
                        SELECT translated_text, translation_source
                          FROM pokemon_text_translation
                         WHERE translation_key = :key
                        """)
                .param("key", keyFactory.cacheKey(kind, sourceLocale, targetLocale, sourceText))
                .query((rs, rowNum) -> new CachedTranslation(rs.getString("translated_text"), rs.getString("translation_source")))
                .optional();
    }

    public Optional<CachedTranslation> findLocal(String locale, String kind, String sourceText) {
        return jdbcClient.sql("""
                        SELECT translated_text, translation_source
                          FROM pokemon_text_translation
                         WHERE translation_key = :key
                        """)
                .param("key", keyFactory.localCacheKey(locale, kind, sourceText))
                .query((rs, rowNum) -> new CachedTranslation(rs.getString("translated_text"), rs.getString("translation_source")))
                .optional();
    }

    public void save(
            String kind,
            String sourceLocale,
            String targetLocale,
            String sourceText,
            String translatedText,
            String translationSource,
            String entityType,
            String entityId
    ) {
        jdbcClient.sql("""
                        MERGE INTO pokemon_text_translation (
                            translation_key, source_text, text_kind, locale, translated_text, translation_source,
                            source_locale, target_locale, text_hash, entity_type, entity_id, updated_at
                        )
                        KEY(translation_key)
                        VALUES (
                            :key, :sourceText, :kind, :locale, :translatedText, :translationSource,
                            :sourceLocale, :targetLocale, :textHash, :entityType, :entityId, CURRENT_TIMESTAMP
                        )
                        """)
                .param("key", keyFactory.cacheKey(kind, sourceLocale, targetLocale, sourceText))
                .param("sourceText", sourceText)
                .param("kind", kind)
                .param("locale", targetLocale)
                .param("translatedText", translatedText)
                .param("translationSource", translationSource)
                .param("sourceLocale", sourceLocale)
                .param("targetLocale", targetLocale)
                .param("textHash", keyFactory.textHash(sourceText))
                .param("entityType", entityType)
                .param("entityId", entityId)
                .update();
    }

    public void saveLocal(String locale, String kind, String sourceText, String translatedText, String translationSource) {
        jdbcClient.sql("""
                        MERGE INTO pokemon_text_translation (
                            translation_key, source_text, text_kind, locale, translated_text, translation_source, updated_at
                        )
                        KEY(translation_key)
                        VALUES (:key, :sourceText, :kind, :locale, :translatedText, :translationSource, CURRENT_TIMESTAMP)
                        """)
                .param("key", keyFactory.localCacheKey(locale, kind, sourceText))
                .param("sourceText", sourceText)
                .param("kind", kind)
                .param("locale", locale)
                .param("translatedText", translatedText)
                .param("translationSource", translationSource)
                .update();
    }

    public int cleanupInvalidCachedTranslations() {
        return jdbcClient.sql("""
                        DELETE FROM pokemon_text_translation
                         WHERE locale IN ('es', 'en')
                           AND (
                                translation_source = 'fallback-source'
                                OR TRIM(translated_text) = TRIM(source_text)
                           )
                        """)
                .update();
    }

    @Schema(description = "Tradução recuperada do cache.")
    public record CachedTranslation(
            @Schema(description = "Texto traduzido salvo.", example = "Um Pokémon rato elétrico.")
            String translatedText,
            @Schema(description = "Origem da tradução salva.", example = "external-provider")
            String translationSource
    ) {
    }
}

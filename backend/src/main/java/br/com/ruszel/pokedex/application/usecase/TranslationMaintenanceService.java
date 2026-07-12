package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.PokemonDetailRepository;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationMaintenanceService {
    public static final String REFRESH_JOB_NAME = "translation-refresh";

    private final JdbcClient jdbcClient;
    private final PokemonDetailRepository pokemonRepository;
    private final TranslationJobStatusService translationJobStatusService;
    private final TranslationCacheService translationCacheService;

    public List<MissingTranslation> findMissingFlavorTexts(int limit) {
        return jdbcClient.sql("""
                        SELECT p.id, p.name
                          FROM pokemon p
                          LEFT JOIN pokemon_species ps ON ps.pokemon_id = p.id
                         WHERE ps.pokemon_id IS NULL
                            OR ps.text_locale IS NULL
                            OR ps.text_locale <> :locale
                            OR ps.flavor_text IS NULL
                            OR TRIM(ps.flavor_text) = ''
                         ORDER BY p.id
                         LIMIT :limit
                        """)
                .param("locale", SpeciesTextLocalizer.CACHE_LOCALE)
                .param("limit", limit)
                .query((rs, rowNum) -> new MissingTranslation(rs.getInt("id"), rs.getString("name")))
                .list();
    }

    public RefreshResult refreshMissingFlavorTexts(int limit) {
        List<MissingTranslation> missing = findMissingFlavorTexts(limit);
        List<MissingTranslation> refreshed = new ArrayList<>();
        List<MissingTranslation> failed = new ArrayList<>();
        translationJobStatusService.start(REFRESH_JOB_NAME, missing.size());

        for (MissingTranslation pokemon : missing) {
            try {
                pokemonRepository.findByNameOrId(String.valueOf(pokemon.id()))
                        .block(Duration.ofSeconds(45));

                if (hasCurrentFlavorText(pokemon.id())) {
                    refreshed.add(pokemon);
                } else {
                    failed.add(pokemon);
                    translationJobStatusService.failOne(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size(), "Translation was not persisted for " + pokemon.name());
                }
            } catch (Exception exception) {
                log.warn("translation_refresh_failed pokemonId={} name={}", pokemon.id(), pokemon.name(), exception);
                failed.add(pokemon);
                translationJobStatusService.failOne(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size(), exception.getMessage());
            }
            translationJobStatusService.progress(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size());
        }

        List<MissingTranslation> remaining = findMissingFlavorTexts(limit);
        translationJobStatusService.finish(REFRESH_JOB_NAME, refreshed.size() + failed.size(), failed.size());
        return new RefreshResult(missing, refreshed, failed, remaining);
    }

    public TranslationJobStatusService.TranslationJobStatus refreshStatus() {
        return translationJobStatusService.current(REFRESH_JOB_NAME)
                .orElse(new TranslationJobStatusService.TranslationJobStatus(REFRESH_JOB_NAME, "IDLE", 0, 0, 0, null, null, null, null));
    }

    public TranslationCacheService.CleanupResult cleanupInvalidCache() {
        return translationCacheService.cleanupInvalidCachedTranslations();
    }

    private boolean hasCurrentFlavorText(int pokemonId) {
        Integer count = jdbcClient.sql("""
                        SELECT COUNT(*)
                          FROM pokemon_species
                         WHERE pokemon_id = :id
                           AND text_locale = :locale
                           AND flavor_text IS NOT NULL
                           AND TRIM(flavor_text) <> ''
                        """)
                .param("id", pokemonId)
                .param("locale", SpeciesTextLocalizer.CACHE_LOCALE)
                .query(Integer.class)
                .single();
        return count > 0;
    }

    public record MissingTranslation(int id, String name) {
    }

    public record RefreshResult(
            List<MissingTranslation> requested,
            List<MissingTranslation> refreshed,
            List<MissingTranslation> failed,
            List<MissingTranslation> remaining
    ) {
    }
}

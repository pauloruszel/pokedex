package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TranslationMaintenanceQueries {
    private final JdbcClient jdbcClient;

    public List<TranslationMaintenanceService.MissingTranslation> findMissingFlavorTexts(int limit) {
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
                .query((rs, rowNum) -> new TranslationMaintenanceService.MissingTranslation(rs.getInt("id"), rs.getString("name")))
                .list();
    }

    public boolean hasCurrentFlavorText(int pokemonId) {
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
}

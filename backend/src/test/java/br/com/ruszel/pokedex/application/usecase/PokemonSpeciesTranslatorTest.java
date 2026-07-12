package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.PokemonSpecies;
import br.com.ruszel.pokedex.infrastructure.localization.PtBrTranslationGateway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonSpeciesTranslatorTest {
    private PokemonSpeciesTranslator translator;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:species-translator-test;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS pokemon_text_translation (
                    translation_key VARCHAR(80) PRIMARY KEY,
                    source_text CLOB NOT NULL,
                    text_kind VARCHAR(40) NOT NULL,
                    locale VARCHAR(20) NOT NULL,
                    translated_text CLOB NOT NULL,
                    translation_source VARCHAR(40) NOT NULL,
                    source_locale VARCHAR(20),
                    target_locale VARCHAR(20),
                    text_hash VARCHAR(80),
                    entity_type VARCHAR(40),
                    entity_id VARCHAR(120),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("DELETE FROM pokemon_text_translation");

        PtBrTranslationGateway gateway = new PtBrTranslationGateway() {
            @Override
            public Optional<String> translate(String sourceText) {
                return Optional.of("traduzido");
            }

            @Override
            public Optional<String> translate(String sourceText, String sourceLanguage, String targetLanguage) {
                return Optional.of(sourceText + " es");
            }
        };
        TranslationCacheService cacheService = new TranslationCacheService(JdbcClient.create(dataSource));
        translator = new PokemonSpeciesTranslator(new TranslateTextUseCase(cacheService, gateway));
    }

    @Test
    void translatesGenusAndFlavorTextKeepingOtherSpeciesFields() {
        PokemonSpecies species = new PokemonSpecies("Mouse Pokemon", "Runs fast", "yellow", "forest", "generation-i");

        PokemonSpecies translated = translator.translate(species, 25, "es");

        assertThat(translated.genus()).isEqualTo("Mouse Pokemon es");
        assertThat(translated.flavorText()).isEqualTo("Runs fast es");
        assertThat(translated.color()).isEqualTo("yellow");
        assertThat(translated.habitat()).isEqualTo("forest");
        assertThat(translated.generation()).isEqualTo("generation-i");
    }
}

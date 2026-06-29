package br.com.ruszel.pokedex.infrastructure.localization;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SpeciesTextLocalizerTest {
    private SpeciesTextLocalizer localizer;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:localizer-test;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS pokemon_text_translation (
                    translation_key VARCHAR(80) PRIMARY KEY,
                    source_text CLOB NOT NULL,
                    text_kind VARCHAR(40) NOT NULL,
                    locale VARCHAR(20) NOT NULL,
                    translated_text CLOB NOT NULL,
                    translation_source VARCHAR(40) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("DELETE FROM pokemon_text_translation");
        localizer = new SpeciesTextLocalizer(
                JdbcClient.create(dataSource),
                new PtBrTranslationGateway() {
                    @Override
                    public Optional<String> translate(String sourceText) {
                        return Optional.of("Quando vários desses Pokémon se reúnem, sua eletricidade pode se acumular e causar tempestades de raios.");
                    }

                    @Override
                    public Optional<String> translate(String sourceText, String sourceLanguage, String targetLanguage) {
                        return translate(sourceText);
                    }
                }
        );
    }

    @Test
    void translatesKnownGenusToPtBrAndCachesIt() {
        String translated = localizer.localizeGenus("Seed Pokémon");

        assertThat(translated).isEqualTo("Pokémon Semente");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pokemon_text_translation", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void normalizesFlavorTextBeforeCaching() {
        String translated = localizer.localizeFlavorText("The plant blooms when it is absorbing solar energy.\nIt stays on the move to seek sunlight.");

        assertThat(translated).contains("planta floresce");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pokemon_text_translation", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void translatesUnknownFlavorTextWithExternalGatewayAndCachesIt() {
        String translated = localizer.localizeFlavorText("When several of these Pokemon gather, their electricity could build and cause lightning storms.");

        assertThat(translated)
                .contains("tempestades de raios")
                .doesNotContain("When several");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pokemon_text_translation", Integer.class);
        assertThat(count).isEqualTo(1);
    }
}

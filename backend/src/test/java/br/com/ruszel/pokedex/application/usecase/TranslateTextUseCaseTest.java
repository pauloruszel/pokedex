package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.infrastructure.localization.PtBrTranslationGateway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TranslateTextUseCaseTest {
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:translate-usecase-test;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
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
    }

    @Test
    void doesNotPersistFallbackSourceAsTranslation() {
        TranslateTextUseCase useCase = new TranslateTextUseCase(
                new TranslationCacheService(JdbcClient.create(jdbcTemplate.getDataSource())),
                gatewayReturning(Optional.empty())
        );

        TranslationResult result = useCase.execute(new TranslationRequest(
                "Texto em português",
                "pt-BR",
                "es",
                "pokemon_flavor_text",
                "pokemon",
                "1"
        ));

        assertThat(result.source()).isEqualTo("untranslated");
        assertThat(result.text()).isBlank();
        assertThat(countTranslations()).isZero();
    }

    @Test
    void persistsTranslationWithSourceTargetLocalesAndEntityMetadata() {
        TranslateTextUseCase useCase = new TranslateTextUseCase(
                new TranslationCacheService(JdbcClient.create(jdbcTemplate.getDataSource())),
                gatewayReturning(Optional.of("Texto en español"))
        );

        TranslationResult result = useCase.execute(new TranslationRequest(
                "Texto em português",
                "pt-BR",
                "es",
                "pokemon_flavor_text",
                "pokemon",
                "25"
        ));

        assertThat(result.text()).isEqualTo("Texto en español");
        assertThat(result.source()).isEqualTo("external-provider");
        assertThat(countTranslations()).isEqualTo(1);

        String sourceLocale = jdbcTemplate.queryForObject("SELECT source_locale FROM pokemon_text_translation", String.class);
        String targetLocale = jdbcTemplate.queryForObject("SELECT target_locale FROM pokemon_text_translation", String.class);
        String entityType = jdbcTemplate.queryForObject("SELECT entity_type FROM pokemon_text_translation", String.class);
        String entityId = jdbcTemplate.queryForObject("SELECT entity_id FROM pokemon_text_translation", String.class);

        assertThat(sourceLocale).isEqualTo("pt-BR");
        assertThat(targetLocale).isEqualTo("es");
        assertThat(entityType).isEqualTo("pokemon");
        assertThat(entityId).isEqualTo("25");
    }

    private PtBrTranslationGateway gatewayReturning(Optional<String> translation) {
        return new PtBrTranslationGateway() {
            @Override
            public Optional<String> translate(String sourceText) {
                return translation;
            }

            @Override
            public Optional<String> translate(String sourceText, String sourceLanguage, String targetLanguage) {
                return translation;
            }
        };
    }

    private int countTranslations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pokemon_text_translation", Integer.class);
    }
}

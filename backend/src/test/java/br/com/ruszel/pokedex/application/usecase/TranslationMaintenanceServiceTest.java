package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.application.port.PokemonRepository;
import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import br.com.ruszel.pokedex.domain.model.PokemonPage;
import br.com.ruszel.pokedex.domain.model.PokemonSpecies;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationMaintenanceServiceTest {
    private JdbcTemplate jdbcTemplate;
    private TranslationMaintenanceService service;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:translation-maintenance-test;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS pokemon (
                    id INT PRIMARY KEY,
                    name VARCHAR(120) NOT NULL UNIQUE
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS pokemon_species (
                    pokemon_id INT PRIMARY KEY,
                    flavor_text CLOB,
                    text_locale VARCHAR(20)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS translation_job_status (
                    job_name VARCHAR(80) PRIMARY KEY,
                    status VARCHAR(30) NOT NULL,
                    total INT NOT NULL DEFAULT 0,
                    processed INT NOT NULL DEFAULT 0,
                    failures INT NOT NULL DEFAULT 0,
                    last_error CLOB,
                    started_at TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    finished_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("DELETE FROM pokemon_species");
        jdbcTemplate.execute("DELETE FROM pokemon");
        jdbcTemplate.execute("DELETE FROM translation_job_status");
        jdbcTemplate.update("INSERT INTO pokemon (id, name) VALUES (25, 'pikachu')");

        JdbcClient jdbcClient = JdbcClient.create(dataSource);
        TranslationJobStatusService statusService = new TranslationJobStatusService(jdbcClient);
        service = new TranslationMaintenanceService(jdbcClient, refreshingRepository(), statusService);
    }

    @Test
    void findsAndRefreshesMissingFlavorTexts() {
        assertThat(service.findMissingFlavorTexts(10)).extracting("name").containsExactly("pikachu");

        TranslationMaintenanceService.RefreshResult result = service.refreshMissingFlavorTexts(10);

        assertThat(result.refreshed()).extracting("name").containsExactly("pikachu");
        assertThat(result.failed()).isEmpty();
        assertThat(service.findMissingFlavorTexts(10)).isEmpty();
        assertThat(service.refreshStatus().status()).isEqualTo("DONE");
    }

    private PokemonRepository refreshingRepository() {
        return new PokemonRepository() {
            @Override
            public Mono<PokemonPage> findAll(int limit, int offset) {
                return Mono.empty();
            }

            @Override
            public Mono<PokemonDetail> findByNameOrId(String nameOrId) {
                jdbcTemplate.update("""
                                MERGE INTO pokemon_species (pokemon_id, flavor_text, text_locale)
                                KEY(pokemon_id)
                                VALUES (25, 'Descrição em pt-BR', ?)
                                """,
                        SpeciesTextLocalizer.CACHE_LOCALE
                );
                return Mono.just(new PokemonDetail(
                        25,
                        "pikachu",
                        null,
                        null,
                        4,
                        60,
                        List.of("electric"),
                        List.of("static"),
                        List.of(),
                        new PokemonSpecies(null, "Descrição em pt-BR", null, null, null),
                        List.of()
                ));
            }

            @Override
            public Flux<String> findTypes() {
                return Flux.empty();
            }

            @Override
            public Mono<PokemonPage> findByType(String typeName, int limit, int offset) {
                return Mono.empty();
            }
        };
    }
}

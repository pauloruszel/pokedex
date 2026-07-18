package br.com.ruszel.pokedex.infrastructure.persistence;

import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers(disabledWithoutDocker = true)
class JdbcPokemonDetailCacheRepositoryPostgresTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pokedex_test")
            .withUsername("pokedex")
            .withPassword("pokedex");

    private JdbcPokemonDetailCacheRepository repository;
    private JdbcClient jdbcClient;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );
        jdbcClient = JdbcClient.create(dataSource);
        recreateSchema();
        seedData();
        repository = new JdbcPokemonDetailCacheRepository(jdbcClient);
    }

    @Test
    void findsPokemonByNumericIdUsingIntegerParameter() {
        Optional<PokemonDetail> result = repository.findDetail("25");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo(25);
        assertThat(result.orElseThrow().name()).isEqualTo("pikachu");
    }

    @Test
    void trimsNumericIdBeforeBindingItAsInteger() {
        Optional<PokemonDetail> result = repository.findDetail("  25  ");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo(25);
    }

    @Test
    void findsPokemonByNameIgnoringCase() {
        Optional<PokemonDetail> result = repository.findDetail("PIKACHU");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo(25);
    }

    @Test
    void returnsEmptyForUnknownNumericIdWithoutPostgresTypeError() {
        assertThatCode(() -> repository.findDetail("9999"))
                .doesNotThrowAnyException();
        assertThat(repository.findDetail("9999")).isEmpty();
    }

    @Test
    void returnsEmptyForNumericValueOutsideIntegerRange() {
        assertThatCode(() -> repository.findDetail("999999999999999999999999"))
                .doesNotThrowAnyException();
        assertThat(repository.findDetail("999999999999999999999999")).isEmpty();
    }

    @Test
    void returnsEmptyForNullBlankAndUnknownName() {
        assertThat(repository.findDetail(null)).isEmpty();
        assertThat(repository.findDetail("   ")).isEmpty();
        assertThat(repository.findDetail("missingno")).isEmpty();
    }

    @Test
    void ignoresCachedDetailWhenSpeciesLocaleIsNotCurrent() {
        assertThat(repository.findDetail("26")).isEmpty();
        assertThat(repository.findDetail("raichu")).isEmpty();
    }

    @Test
    void ignoresIncompleteCachedDetailWithoutHeight() {
        assertThat(repository.findDetail("27")).isEmpty();
        assertThat(repository.findDetail("sandshrew")).isEmpty();
    }

    private void recreateSchema() {
        jdbcClient.sql("DROP TABLE IF EXISTS pokemon_evolution").update();
        jdbcClient.sql("DROP TABLE IF EXISTS pokemon_stat").update();
        jdbcClient.sql("DROP TABLE IF EXISTS pokemon_ability").update();
        jdbcClient.sql("DROP TABLE IF EXISTS pokemon_type").update();
        jdbcClient.sql("DROP TABLE IF EXISTS pokemon_species").update();
        jdbcClient.sql("DROP TABLE IF EXISTS pokemon").update();

        jdbcClient.sql("""
                CREATE TABLE pokemon (
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(120) NOT NULL UNIQUE,
                    image_url VARCHAR(500),
                    sprite_url VARCHAR(500),
                    height INTEGER,
                    weight INTEGER,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pokemon_species (
                    pokemon_id INTEGER PRIMARY KEY REFERENCES pokemon(id),
                    genus VARCHAR(255),
                    flavor_text TEXT,
                    text_locale VARCHAR(20),
                    color VARCHAR(80),
                    habitat VARCHAR(80),
                    generation VARCHAR(80)
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pokemon_type (
                    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
                    type_name VARCHAR(80) NOT NULL,
                    slot_order INTEGER NOT NULL,
                    PRIMARY KEY (pokemon_id, type_name)
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pokemon_ability (
                    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
                    ability_name VARCHAR(120) NOT NULL,
                    slot_order INTEGER NOT NULL,
                    PRIMARY KEY (pokemon_id, ability_name)
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pokemon_stat (
                    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
                    stat_name VARCHAR(120) NOT NULL,
                    stat_value INTEGER NOT NULL,
                    PRIMARY KEY (pokemon_id, stat_name)
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE pokemon_evolution (
                    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
                    evolution_name VARCHAR(120) NOT NULL,
                    chain_order INTEGER NOT NULL,
                    PRIMARY KEY (pokemon_id, evolution_name, chain_order)
                )
                """).update();
    }

    private void seedData() {
        insertPokemon(25, "pikachu", 4, 60, SpeciesTextLocalizer.CACHE_LOCALE);
        insertPokemon(26, "raichu", 8, 300, "en");
        insertPokemon(27, "sandshrew", null, 120, SpeciesTextLocalizer.CACHE_LOCALE);

        jdbcClient.sql("INSERT INTO pokemon_type (pokemon_id, type_name, slot_order) VALUES (25, 'electric', 1)").update();
        jdbcClient.sql("INSERT INTO pokemon_ability (pokemon_id, ability_name, slot_order) VALUES (25, 'static', 1)").update();
        jdbcClient.sql("INSERT INTO pokemon_stat (pokemon_id, stat_name, stat_value) VALUES (25, 'speed', 90)").update();
        jdbcClient.sql("INSERT INTO pokemon_evolution (pokemon_id, evolution_name, chain_order) VALUES (25, 'pichu', 1)").update();
        jdbcClient.sql("INSERT INTO pokemon_evolution (pokemon_id, evolution_name, chain_order) VALUES (25, 'pikachu', 2)").update();
        jdbcClient.sql("INSERT INTO pokemon_evolution (pokemon_id, evolution_name, chain_order) VALUES (25, 'raichu', 3)").update();
    }

    private void insertPokemon(int id, String name, Integer height, int weight, String locale) {
        jdbcClient.sql("""
                INSERT INTO pokemon (id, name, image_url, sprite_url, height, weight)
                VALUES (:id, :name, :imageUrl, :spriteUrl, :height, :weight)
                """)
                .param("id", id)
                .param("name", name)
                .param("imageUrl", "/images/" + id + ".png")
                .param("spriteUrl", "/sprites/" + id + ".png")
                .param("height", height)
                .param("weight", weight)
                .update();

        jdbcClient.sql("""
                INSERT INTO pokemon_species (pokemon_id, genus, flavor_text, text_locale, color, habitat, generation)
                VALUES (:id, 'Mouse Pokemon', 'Test flavor text', :locale, 'yellow', 'forest', 'generation-i')
                """)
                .param("id", id)
                .param("locale", locale)
                .update();
    }
}

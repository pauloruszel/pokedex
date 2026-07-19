package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.TrunfoRoomView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Testcontainers(disabledWithoutDocker = true)
class TrunfoRoomServicePostgresTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("pokedex_room_test")
            .withUsername("pokedex")
            .withPassword("pokedex");

    private JdbcClient jdbcClient;
    private TrunfoRoomService service;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );
        jdbcClient = JdbcClient.create(dataSource);
        recreateSchema();
        service = new TrunfoRoomService(jdbcClient, mock(ListTrunfoCardsUseCase.class));
    }

    @Test
    void createsRoomWithoutTypeFilter() {
        TrunfoRoomView room = service.create("Paulo", "all", "balanced", null, "manual", 8);

        assertThat(room.code()).startsWith("PKM-");
        assertThat(room.state()).isEqualTo("WAITING_FOR_PLAYER");
        assertThat(room.type()).isNull();
        assertThat(room.deckSelection()).isEqualTo("manual");
        assertThat(room.deckSize()).isEqualTo(8);
    }

    @Test
    void treatsBlankTypeAsNull() {
        TrunfoRoomView room = service.create("Paulo", "all", "balanced", "   ", "auto", 20);

        assertThat(room.type()).isNull();
        assertThat(room.deckSelection()).isEqualTo("auto");
        assertThat(room.deckSize()).isEqualTo(20);
    }

    @Test
    void trimsAndPersistsTypeFilter() {
        TrunfoRoomView room = service.create("Paulo", "type", "balanced", " electric ", "manual", 8);

        assertThat(room.type()).isEqualTo("electric");
    }

    private void recreateSchema() {
        jdbcClient.sql("DROP TABLE IF EXISTS trunfo_room").update();
        jdbcClient.sql("""
                CREATE TABLE trunfo_room (
                    code VARCHAR(12) PRIMARY KEY,
                    state VARCHAR(40) NOT NULL,
                    mode VARCHAR(40) NOT NULL,
                    difficulty VARCHAR(40) NOT NULL,
                    deck_selection VARCHAR(40) NOT NULL DEFAULT 'auto',
                    deck_size INTEGER NOT NULL DEFAULT 8,
                    type_name VARCHAR(80),
                    player_one_name VARCHAR(80) NOT NULL,
                    player_two_name VARCHAR(80),
                    player_one_token VARCHAR(80) NOT NULL,
                    player_two_token VARCHAR(80),
                    player_one_deck TEXT,
                    player_two_deck TEXT,
                    dispute_pile TEXT,
                    history TEXT,
                    current_turn VARCHAR(20) NOT NULL,
                    round_number INTEGER NOT NULL DEFAULT 1,
                    winner VARCHAR(80),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP
                )
                """).update();
    }
}

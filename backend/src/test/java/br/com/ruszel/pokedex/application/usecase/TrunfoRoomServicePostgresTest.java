package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.TrunfoAttributes;
import br.com.ruszel.pokedex.domain.model.TrunfoCard;
import br.com.ruszel.pokedex.domain.model.TrunfoRoomView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Testcontainers(disabledWithoutDocker = true)
class TrunfoRoomServicePostgresTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    @Test
    void playsNonFinalRoundWithNullWinner() throws Exception {
        TrunfoCard stronger = card(1, "bulbasaur", 70);
        TrunfoCard reserveOne = card(2, "ivysaur", 60);
        TrunfoCard weaker = card(4, "charmander", 40);
        TrunfoCard reserveTwo = card(5, "charmeleon", 50);

        jdbcClient.sql("""
                INSERT INTO trunfo_room (
                    code, state, mode, difficulty, deck_selection, deck_size,
                    player_one_name, player_two_name, player_one_token, player_two_token,
                    player_one_deck, player_two_deck, dispute_pile, history,
                    current_turn, round_number, expires_at
                ) VALUES (
                    'PKM-9999', 'IN_PROGRESS', 'all', 'balanced', 'auto', 8,
                    'Paulo', 'Joao', 'token-1', 'token-2',
                    :playerOneDeck, :playerTwoDeck, '[]', '[]',
                    'player-one', 1, CURRENT_TIMESTAMP
                )
                """)
                .param("playerOneDeck", OBJECT_MAPPER.writeValueAsString(List.of(stronger, reserveOne)))
                .param("playerTwoDeck", OBJECT_MAPPER.writeValueAsString(List.of(weaker, reserveTwo)))
                .update();

        TrunfoRoomView room = service.playRound("PKM-9999", "token-1", "attack");

        assertThat(room.state()).isEqualTo("IN_PROGRESS");
        assertThat(room.winner()).isNull();
        assertThat(room.round()).isEqualTo(2);
    }

    private TrunfoCard card(int id, String name, int attack) {
        return new TrunfoCard(
                id,
                name,
                "/images/" + id + ".png",
                List.of("normal"),
                "common",
                false,
                new TrunfoAttributes(50, attack, 50, 50, 50, 50, 10.0, 1.0, 300)
        );
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

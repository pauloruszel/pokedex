package br.com.ruszel.pokedex.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "pokedex.bootstrap.enabled=false",
        "pokedex.translation.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:trunfo-room-controller-test;DB_CLOSE_DELAY=-1"
})
class TrunfoRoomControllerTest {

    private WebTestClient webTestClient;

    @Value("${local.server.port}")
    private int port;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Test
    void createsPrivateRoomAndRejectsPlayerOutOfTurn() {
        String createBody = """
                {"nickname":"Ash","mode":"all","difficulty":"balanced","deckSize":8}
                """;
        Map<?, ?> created = webTestClient.post()
                .uri("/api/trunfo/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        String code = (String) created.get("code");
        Map<?, ?> joined = webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/join", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nickname\":\"Misty\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        assertThat(joined.get("playerDeckCount")).isEqualTo(8);
        assertThat(joined.get("opponentDeckCount")).isEqualTo(8);

        webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/rounds", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("playerToken", joined.get("playerToken"), "attribute", "speed"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void manualRoomWaitsForDeckSelectionAndValidatesDeckSize() {
        Map<?, ?> created = webTestClient.post()
                .uri("/api/trunfo/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"nickname":"Ash","mode":"all","difficulty":"balanced","deckSelection":"manual","deckSize":8}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        String code = (String) created.get("code");
        Map<?, ?> joined = webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/join", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nickname\":\"Misty\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(joined.get("state")).isEqualTo("DECK_SELECTION");

        webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/deck", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("playerToken", joined.get("playerToken"), "cardIds", List.of(1)))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void manualRoomStartsAfterBothPlayersConfirmDifferentDecks() {
        Map<?, ?> created = webTestClient.post()
                .uri("/api/trunfo/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"nickname":"Ash","mode":"all","difficulty":"balanced","deckSelection":"manual","deckSize":8}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        String code = (String) created.get("code");
        String playerOneToken = (String) created.get("playerToken");
        Map<?, ?> joined = webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/join", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nickname\":\"Misty\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/deck", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("playerToken", playerOneToken, "cardIds", List.of(1, 2, 3, 4, 5, 6, 7, 8)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.state").isEqualTo("DECK_SELECTION");

        webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/deck", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("playerToken", joined.get("playerToken"), "cardIds", List.of(1, 2, 3, 4, 5, 6, 7, 8)))
                .exchange()
                .expectStatus().isBadRequest();

        webTestClient.post()
                .uri("/api/trunfo/rooms/{code}/deck", code)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("playerToken", joined.get("playerToken"), "cardIds", List.of(9, 10, 11, 12, 13, 14, 15, 16)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.state").isEqualTo("IN_PROGRESS")
                .jsonPath("$.playerDeckCount").isEqualTo(8)
                .jsonPath("$.opponentDeckCount").isEqualTo(8);
    }
}

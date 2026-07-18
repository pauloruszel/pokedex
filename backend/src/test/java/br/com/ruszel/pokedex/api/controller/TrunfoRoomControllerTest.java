package br.com.ruszel.pokedex.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
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
}

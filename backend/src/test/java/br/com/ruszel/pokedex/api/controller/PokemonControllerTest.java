package br.com.ruszel.pokedex.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "pokedex.bootstrap.enabled=false",
        "pokedex.admin.token=test-token",
        "spring.datasource.url=jdbc:h2:mem:pokedex-controller-test;DB_CLOSE_DELAY=-1"
})
class PokemonControllerTest {

    private WebTestClient webTestClient;

    @Value("${local.server.port}")
    private int port;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void returnsReferenceTypesAsJsonArray() {
        webTestClient.get()
                .uri("/api/pokemon/types")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0]").isEqualTo("normal")
                .jsonPath("$[2]").isEqualTo("water");
    }

    @Test
    void rejectsAdminEndpointWithoutToken() {
        webTestClient.get()
                .uri("/api/admin/translations/missing")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void allowsAdminEndpointWithToken() {
        webTestClient.get()
                .uri("/api/admin/translations/missing")
                .header("X-Admin-Token", "test-token")
                .exchange()
                .expectStatus().isOk();
    }
}

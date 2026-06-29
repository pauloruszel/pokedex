package br.com.ruszel.pokedex.infrastructure.pokeapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
@Slf4j
public class PokeApiClient {
    private final WebClient pokeApiWebClient;
    private final WebClient.Builder webClientBuilder;

    public Mono<JsonNode> listPokemon(int limit, int offset) {
        long started = System.nanoTime();
        return pokeApiWebClient.get()
                .uri(uri -> uri.path("/pokemon").queryParam("limit", limit).queryParam("offset", offset).build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doFinally(signal -> log.debug("pokeapi_call endpoint=listPokemon durationMs={}", elapsedMs(started)));
    }

    public Mono<JsonNode> getPokemon(String nameOrId) {
        long started = System.nanoTime();
        return pokeApiWebClient.get()
                .uri("/pokemon/{nameOrId}", nameOrId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doFinally(signal -> log.debug("pokeapi_call endpoint=getPokemon id={} durationMs={}", nameOrId, elapsedMs(started)));
    }

    public Mono<JsonNode> getType(String typeName) {
        long started = System.nanoTime();
        return pokeApiWebClient.get()
                .uri("/type/{typeName}", typeName)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doFinally(signal -> log.debug("pokeapi_call endpoint=getType type={} durationMs={}", typeName, elapsedMs(started)));
    }

    public Mono<JsonNode> getAbsolute(String url) {
        long started = System.nanoTime();
        return webClientBuilder.clone().build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doFinally(signal -> log.debug("pokeapi_call endpoint=absolute durationMs={}", elapsedMs(started)));
    }

    private long elapsedMs(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}

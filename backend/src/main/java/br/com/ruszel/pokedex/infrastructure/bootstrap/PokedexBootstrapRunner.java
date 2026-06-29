package br.com.ruszel.pokedex.infrastructure.bootstrap;

import br.com.ruszel.pokedex.application.port.PokemonRepository;
import br.com.ruszel.pokedex.application.usecase.CachePokemonImageUseCase;
import br.com.ruszel.pokedex.application.usecase.TranslationJobStatusService;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PokedexBootstrapRunner implements CommandLineRunner {
    private static final Pattern ID_FROM_URL = Pattern.compile("/pokemon/(\\d+)/?$");

    private final JdbcClient jdbcClient;
    private final WebClient pokeApiWebClient;
    private final CachePokemonImageUseCase cachePokemonImageUseCase;
    private final PokemonRepository pokemonRepository;
    private final TranslationJobStatusService translationJobStatusService;
    private final TaskExecutor pokedexTaskExecutor;

    @Value("${pokedex.bootstrap.enabled:true}")
    private boolean enabled;

    @Value("${pokedex.bootstrap.limit:1025}")
    private int limit;

    @Value("${pokedex.bootstrap.details-enabled:true}")
    private boolean detailsEnabled;

    @Override
    public void run(String... args) {
        if (!enabled) {
            log.info("Pokemon catalog bootstrap is disabled.");
            return;
        }

        pokedexTaskExecutor.execute(this::bootstrapCatalog);
        log.info("Pokemon catalog bootstrap scheduled in background.");
    }

    @Transactional
    public void bootstrapCatalog() {

        Integer currentCount = jdbcClient.sql("SELECT COUNT(*) FROM pokemon")
                .query(Integer.class)
                .single();

        if (currentCount >= limit) {
            log.info("Pokemon catalog already has {} records. Skipping bootstrap.", currentCount);
            bootstrapDetails();
            return;
        }

        if (currentCount > 0) {
            log.info("Pokemon catalog has {} of {} expected records. Completing bootstrap.", currentCount, limit);
        }

        JsonNode page;
        try {
            page = pokeApiWebClient.get()
                    .uri(uri -> uri.path("/pokemon").queryParam("limit", limit).queryParam("offset", 0).build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(30));
        } catch (Exception exception) {
            log.warn("Pokemon catalog bootstrap failed. The backend will continue using cached data and on-demand PokeAPI calls.", exception);
            return;
        }

        if (page == null || !page.has("results")) {
            log.warn("Pokemon catalog bootstrap did not receive results from PokeAPI.");
            return;
        }

        int inserted = 0;
        for (JsonNode item : page.get("results")) {
            String name = item.get("name").asText();
            String sourceUrl = item.get("url").asText();
            Integer id = extractId(sourceUrl);
            if (id == null) {
                continue;
            }

            String officialArtworkUrl = officialArtworkUrl(id);
            String frontSpriteUrl = frontSpriteUrl(id);
            String internalOfficialArtworkUrl = cachePokemonImageUseCase.execute(id, "official-artwork", officialArtworkUrl);
            String internalFrontSpriteUrl = cachePokemonImageUseCase.execute(id, "front-default", frontSpriteUrl);

            jdbcClient.sql("""
                            MERGE INTO pokemon (id, name, image_url, sprite_url, source_url, updated_at)
                            KEY(id)
                            VALUES (:id, :name, :imageUrl, :spriteUrl, :sourceUrl, CURRENT_TIMESTAMP)
                            """)
                    .param("id", id)
                    .param("name", name)
                    .param("imageUrl", internalOfficialArtworkUrl)
                    .param("spriteUrl", internalFrontSpriteUrl)
                    .param("sourceUrl", sourceUrl)
                    .update();
            inserted++;
            if (inserted % 100 == 0) {
                log.info("Pokemon catalog bootstrap progress: {} records.", inserted);
            }
        }

        log.info("Pokemon catalog bootstrap finished with {} catalog records.", inserted);

        bootstrapDetails();
    }

    private void bootstrapDetails() {
        if (!detailsEnabled) {
            log.info("Pokemon details/species bootstrap is disabled.");
            return;
        }

        Integer translatedCount = jdbcClient.sql("""
                        SELECT COUNT(*)
                          FROM pokemon_species
                         WHERE text_locale = :locale
                        """)
                .param("locale", SpeciesTextLocalizer.CACHE_LOCALE)
                .query(Integer.class)
                .single();

        if (translatedCount >= limit) {
            log.info("Pokemon details/species cache already has {} translated records. Skipping.", translatedCount);
            return;
        }

        var names = jdbcClient.sql("""
                        SELECT name
                          FROM pokemon
                         ORDER BY id
                         LIMIT :limit
                        """)
                .param("limit", limit)
                .query(String.class)
                .list();

        log.info("Pokemon details/species bootstrap started for {} records. Already translated: {}.", names.size(), translatedCount);
        translationJobStatusService.start("bootstrap-details", names.size());
        int processed = 0;
        int translated = 0;
        int failures = 0;
        for (String name : names) {
            try {
                pokemonRepository.findByNameOrId(name).block(Duration.ofSeconds(45));
                translated++;
            } catch (Exception exception) {
                failures++;
                log.warn("Pokemon details/species bootstrap failed for {}. It will be retried on a future run.", name, exception);
                translationJobStatusService.failOne("bootstrap-details", processed + 1, failures, exception.getMessage());
            }

            processed++;
            translationJobStatusService.progress("bootstrap-details", processed, failures);
            if (processed % 25 == 0) {
                log.info("Pokemon details/species bootstrap progress: {}/{} processed, {} saved.", processed, names.size(), translated);
            }
        }

        translationJobStatusService.finish("bootstrap-details", processed, failures);
        log.info("Pokemon details/species bootstrap finished: {}/{} saved.", translated, names.size());
    }

    private Integer extractId(String url) {
        var matcher = ID_FROM_URL.matcher(url);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

    private String officialArtworkUrl(Integer id) {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
    }

    private String frontSpriteUrl(Integer id) {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";
    }
}

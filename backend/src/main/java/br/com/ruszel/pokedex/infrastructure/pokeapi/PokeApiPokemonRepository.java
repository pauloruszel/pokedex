package br.com.ruszel.pokedex.infrastructure.pokeapi;

import br.com.ruszel.pokedex.application.port.PokemonRepository;
import br.com.ruszel.pokedex.domain.model.*;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PokeApiPokemonRepository implements PokemonRepository {
    private static final String SPECIES_TEXT_CACHE_LOCALE = SpeciesTextLocalizer.CACHE_LOCALE;
    private final PokeApiClient pokeApiClient;
    private final PokeApiPokemonMapper pokemonMapper;
    private final JdbcClient jdbcClient;

    @Override
    public Mono<PokemonPage> findAll(int limit, int offset) {
        return Mono.fromSupplier(() -> findCachedPage(limit, offset))
                .filter(this::hasTypedResults)
                .switchIfEmpty(fetchRemotePage(limit, offset));
    }

    @Override
    public Mono<PokemonDetail> findByNameOrId(String nameOrId) {
        return Mono.fromSupplier(() -> findCachedDetail(nameOrId))
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty))
                .switchIfEmpty(fetchPokemon(nameOrId)
                        .flatMap(pokemon -> fetchSpecies(speciesUrl(pokemon))
                                .switchIfEmpty(Mono.just(NullNode.getInstance()))
                                .flatMap(species -> fetchEvolutionChain(species)
                                        .collectList()
                                        .publishOn(Schedulers.boundedElastic())
                                        .map(chain -> pokemonMapper.toDetail(pokemon, species, chain))))
                        .doOnNext(this::saveDetail));
    }

    @Override
    public Flux<String> findTypes() {
        return Mono.fromSupplier(() -> jdbcClient.sql("SELECT name FROM pokemon_reference_type ORDER BY display_order")
                        .query(String.class)
                        .list())
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<PokemonPage> findByType(String typeName, int limit, int offset) {
        return Mono.fromSupplier(() -> findCachedPageByType(typeName, limit, offset))
                .filter(this::hasTypedResults)
                .switchIfEmpty(fetchRemotePageByType(typeName, limit, offset));
    }


    private boolean hasTypedResults(PokemonPage page) {
        return page != null
                && page.results() != null
                && !page.results().isEmpty()
                && page.results().stream().allMatch(result -> result.types() != null && !result.types().isEmpty());
    }

    private PokemonPage findCachedPage(int limit, int offset) {
        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM pokemon")
                .query(Integer.class)
                .single();
        List<PokemonSummary> results = jdbcClient.sql("""
                        SELECT id, name, image_url
                          FROM pokemon
                         ORDER BY id
                         LIMIT :limit OFFSET :offset
                        """)
                .param("limit", limit)
                .param("offset", offset)
                .query((rs, rowNum) -> new PokemonSummary(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("image_url"),
                        findCachedTypes(rs.getInt("id"))
                ))
                .list();
        return new PokemonPage(count, limit, offset, results);
    }

    private PokemonPage findCachedPageByType(String typeName, int limit, int offset) {
        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM pokemon_type WHERE type_name = :type")
                .param("type", typeName)
                .query(Integer.class)
                .single();
        List<PokemonSummary> results = jdbcClient.sql("""
                        SELECT p.id, p.name, p.image_url
                          FROM pokemon p
                          JOIN pokemon_type pt ON pt.pokemon_id = p.id
                         WHERE pt.type_name = :type
                         ORDER BY p.id
                         LIMIT :limit OFFSET :offset
                        """)
                .param("type", typeName)
                .param("limit", limit)
                .param("offset", offset)
                .query((rs, rowNum) -> new PokemonSummary(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("image_url"),
                        findCachedTypes(rs.getInt("id"))
                ))
                .list();
        return new PokemonPage(count, limit, offset, results);
    }

    private Optional<PokemonDetail> findCachedDetail(String nameOrId) {
        String sql = nameOrId.matches("\\d+")
                ? """
                SELECT id, name, image_url, sprite_url, height, weight
                  FROM pokemon
                 WHERE id = :value
                   AND height IS NOT NULL
                   AND EXISTS (
                       SELECT 1 FROM pokemon_species ps
                        WHERE ps.pokemon_id = pokemon.id
                          AND ps.text_locale = :locale
                   )
                """
                : """
                SELECT id, name, image_url, sprite_url, height, weight
                  FROM pokemon
                 WHERE LOWER(name) = LOWER(:value)
                   AND height IS NOT NULL
                   AND EXISTS (
                       SELECT 1 FROM pokemon_species ps
                        WHERE ps.pokemon_id = pokemon.id
                          AND ps.text_locale = :locale
                   )
                """;

        return jdbcClient.sql(sql)
                .param("value", nameOrId)
                .param("locale", SPECIES_TEXT_CACHE_LOCALE)
                .query((rs, rowNum) -> new PokemonDetail(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("image_url"),
                        rs.getString("sprite_url"),
                        rs.getInt("height"),
                        rs.getInt("weight"),
                        findCachedTypes(rs.getInt("id")),
                        findCachedAbilities(rs.getInt("id")),
                        findCachedStats(rs.getInt("id")),
                        findCachedSpecies(rs.getInt("id")),
                        findCachedEvolutionChain(rs.getInt("id"))
                ))
                .optional();
    }

    private List<String> findCachedTypes(int pokemonId) {
        return jdbcClient.sql("SELECT type_name FROM pokemon_type WHERE pokemon_id = :id ORDER BY slot_order")
                .param("id", pokemonId)
                .query(String.class)
                .list();
    }

    private List<String> findCachedAbilities(int pokemonId) {
        return jdbcClient.sql("SELECT ability_name FROM pokemon_ability WHERE pokemon_id = :id ORDER BY slot_order")
                .param("id", pokemonId)
                .query(String.class)
                .list();
    }

    private List<PokemonStat> findCachedStats(int pokemonId) {
        return jdbcClient.sql("SELECT stat_name, stat_value FROM pokemon_stat WHERE pokemon_id = :id")
                .param("id", pokemonId)
                .query((rs, rowNum) -> new PokemonStat(rs.getString("stat_name"), rs.getInt("stat_value")))
                .list();
    }

    private PokemonSpecies findCachedSpecies(int pokemonId) {
        return jdbcClient.sql("SELECT genus, flavor_text, color, habitat, generation FROM pokemon_species WHERE pokemon_id = :id")
                .param("id", pokemonId)
                .query((rs, rowNum) -> new PokemonSpecies(
                        rs.getString("genus"),
                        rs.getString("flavor_text"),
                        rs.getString("color"),
                        rs.getString("habitat"),
                        rs.getString("generation")
                ))
                .optional()
                .orElse(new PokemonSpecies(null, null, null, null, null));
    }

    private List<String> findCachedEvolutionChain(int pokemonId) {
        return jdbcClient.sql("SELECT evolution_name FROM pokemon_evolution WHERE pokemon_id = :id ORDER BY chain_order")
                .param("id", pokemonId)
                .query(String.class)
                .list();
    }

    private Mono<PokemonPage> fetchRemotePage(int limit, int offset) {
        return pokeApiClient.listPokemon(limit, offset)
                .flatMap(page -> Flux.fromIterable(page.get("results"))
                        .concatMap(item -> findSummaryByName(item.get("name").asText()))
                        .collectList()
                        .doOnNext(results -> results.forEach(this::saveSummary))
                        .map(results -> new PokemonPage(page.get("count").asInt(), limit, offset, results)));
    }

    private Mono<PokemonPage> fetchRemotePageByType(String typeName, int limit, int offset) {
        return pokeApiClient.getType(typeName)
                .flatMap(type -> {
                    List<JsonNode> all = new ArrayList<>();
                    type.get("pokemon").forEach(all::add);
                    var slice = all.stream().skip(offset).limit(limit).toList();
                    return Flux.fromIterable(slice)
                            .map(item -> item.get("pokemon").get("name").asText())
                            .concatMap(this::findSummaryByName)
                            .collectList()
                            .doOnNext(results -> results.forEach(this::saveSummary))
                            .map(results -> new PokemonPage(all.size(), limit, offset, results));
                });
    }

    private Mono<PokemonSummary> findSummaryByName(String name) {
        return fetchPokemon(name)
                .publishOn(Schedulers.boundedElastic())
                .map(pokemonMapper::toSummary);
    }

    private Mono<JsonNode> fetchPokemon(String nameOrId) {
        return pokeApiClient.getPokemon(nameOrId);
    }

    private Mono<JsonNode> fetchSpecies(String url) {
        if (url == null || url.isBlank()) {
            return Mono.empty();
        }
        return pokeApiClient.getAbsolute(url);
    }

    private Flux<String> fetchEvolutionChain(JsonNode species) {
        if (species == null || species.isNull() || species.path("evolution_chain").path("url").isMissingNode()) {
            return Flux.empty();
        }
        return pokeApiClient.getAbsolute(species.get("evolution_chain").get("url").asText())
                .flatMapMany(node -> Flux.fromIterable(extractEvolutionNames(node.get("chain"))));
    }

    @Transactional
    public void saveSummary(PokemonSummary pokemon) {
        jdbcClient.sql("""
                        MERGE INTO pokemon (id, name, image_url, sprite_url, updated_at)
                        KEY(id)
                        VALUES (:id, :name, :imageUrl, NULL, CURRENT_TIMESTAMP)
                        """)
                .param("id", pokemon.id())
                .param("name", pokemon.name())
                .param("imageUrl", pokemon.imageUrl())
                .update();
        saveTypes(pokemon.id(), pokemon.types());
    }

    @Transactional
    public void saveDetail(PokemonDetail pokemon) {
        jdbcClient.sql("""
                        MERGE INTO pokemon (id, name, image_url, sprite_url, height, weight, updated_at)
                        KEY(id)
                        VALUES (:id, :name, :imageUrl, :spriteUrl, :height, :weight, CURRENT_TIMESTAMP)
                        """)
                .param("id", pokemon.id())
                .param("name", pokemon.name())
                .param("imageUrl", pokemon.imageUrl())
                .param("spriteUrl", pokemon.spriteUrl())
                .param("height", pokemon.height())
                .param("weight", pokemon.weight())
                .update();

        saveTypes(pokemon.id(), pokemon.types());
        replaceAbilities(pokemon);
        replaceStats(pokemon);
        replaceSpecies(pokemon);
        replaceEvolutionChain(pokemon);
    }

    private void saveTypes(Integer pokemonId, List<String> types) {
        if (types == null || types.isEmpty()) {
            return;
        }
        int order = 1;
        for (String type : types) {
            jdbcClient.sql("MERGE INTO pokemon_type (pokemon_id, type_name, slot_order) KEY(pokemon_id, type_name) VALUES (:id, :type, :order)")
                    .param("id", pokemonId)
                    .param("type", type)
                    .param("order", order++)
                    .update();
        }
    }

    private void replaceAbilities(PokemonDetail pokemon) {
        jdbcClient.sql("DELETE FROM pokemon_ability WHERE pokemon_id = :id").param("id", pokemon.id()).update();
        int order = 1;
        for (String ability : pokemon.abilities()) {
            jdbcClient.sql("INSERT INTO pokemon_ability (pokemon_id, ability_name, slot_order) VALUES (:id, :ability, :order)")
                    .param("id", pokemon.id()).param("ability", ability).param("order", order++).update();
        }
    }

    private void replaceStats(PokemonDetail pokemon) {
        jdbcClient.sql("DELETE FROM pokemon_stat WHERE pokemon_id = :id").param("id", pokemon.id()).update();
        for (PokemonStat stat : pokemon.stats()) {
            jdbcClient.sql("INSERT INTO pokemon_stat (pokemon_id, stat_name, stat_value) VALUES (:id, :name, :value)")
                    .param("id", pokemon.id()).param("name", stat.name()).param("value", stat.value()).update();
        }
    }

    private void replaceSpecies(PokemonDetail pokemon) {
        jdbcClient.sql("""
                        MERGE INTO pokemon_species (pokemon_id, genus, flavor_text, text_locale, color, habitat, generation)
                        KEY(pokemon_id)
                        VALUES (:id, :genus, :flavorText, :textLocale, :color, :habitat, :generation)
                        """)
                .param("id", pokemon.id())
                .param("genus", pokemon.species().genus())
                .param("flavorText", pokemon.species().flavorText())
                .param("textLocale", pokemon.species().flavorText() == null ? null : SPECIES_TEXT_CACHE_LOCALE)
                .param("color", pokemon.species().color())
                .param("habitat", pokemon.species().habitat())
                .param("generation", pokemon.species().generation())
                .update();
    }

    private void replaceEvolutionChain(PokemonDetail pokemon) {
        jdbcClient.sql("DELETE FROM pokemon_evolution WHERE pokemon_id = :id").param("id", pokemon.id()).update();
        int order = 1;
        for (String evolutionName : pokemon.evolutionChain()) {
            jdbcClient.sql("INSERT INTO pokemon_evolution (pokemon_id, evolution_name, chain_order) VALUES (:id, :name, :order)")
                    .param("id", pokemon.id()).param("name", evolutionName).param("order", order++).update();
        }
    }

    private String speciesUrl(JsonNode pokemon) {
        return pokemon.path("species").path("url").asText(null);
    }

    private List<String> extractEvolutionNames(JsonNode chain) {
        var names = new ArrayList<String>();
        collectEvolutionNames(chain, names);
        return names;
    }

    private void collectEvolutionNames(JsonNode current, List<String> names) {
        if (current == null || current.isMissingNode()) {
            return;
        }
        names.add(current.path("species").path("name").asText());
        current.path("evolves_to").forEach(next -> collectEvolutionNames(next, names));
    }
}

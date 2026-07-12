package br.com.ruszel.pokedex.infrastructure.pokeapi;

import br.com.ruszel.pokedex.application.port.PokemonRepository;
import br.com.ruszel.pokedex.domain.model.*;
import br.com.ruszel.pokedex.infrastructure.persistence.JdbcPokemonCatalogCacheRepository;
import br.com.ruszel.pokedex.infrastructure.persistence.JdbcPokemonDetailCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PokeApiPokemonRepository implements PokemonRepository {
    private final PokeApiClient pokeApiClient;
    private final PokeApiPokemonSummaryMapper summaryMapper;
    private final PokeApiPokemonDetailMapper detailMapper;
    private final JdbcPokemonCatalogCacheRepository catalogCacheRepository;
    private final JdbcPokemonDetailCacheRepository detailCacheRepository;

    @Override
    public Mono<PokemonPage> findAll(int limit, int offset) {
        return Mono.fromSupplier(() -> catalogCacheRepository.findPage(limit, offset))
                .filter(this::hasTypedResults)
                .switchIfEmpty(fetchRemotePage(limit, offset));
    }

    @Override
    public Mono<PokemonDetail> findByNameOrId(String nameOrId) {
        return Mono.fromSupplier(() -> detailCacheRepository.findDetail(nameOrId))
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty))
                .switchIfEmpty(fetchPokemon(nameOrId)
                        .flatMap(pokemon -> fetchSpecies(speciesUrl(pokemon))
                                .switchIfEmpty(Mono.just(NullNode.getInstance()))
                                .flatMap(species -> fetchEvolutionChain(species)
                                        .collectList()
                                        .publishOn(Schedulers.boundedElastic())
                                        .map(chain -> detailMapper.toDetail(pokemon, species, chain))))
                        .doOnNext(detailCacheRepository::saveDetail));
    }

    @Override
    public Flux<String> findTypes() {
        return Mono.fromSupplier(catalogCacheRepository::findTypes)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<PokemonPage> findByType(String typeName, int limit, int offset) {
        return Mono.fromSupplier(() -> catalogCacheRepository.findPageByType(typeName, limit, offset))
                .filter(this::hasTypedResults)
                .switchIfEmpty(fetchRemotePageByType(typeName, limit, offset));
    }


    private boolean hasTypedResults(PokemonPage page) {
        return page != null
                && page.results() != null
                && !page.results().isEmpty()
                && page.results().stream().allMatch(result -> result.types() != null && !result.types().isEmpty());
    }

    private Mono<PokemonPage> fetchRemotePage(int limit, int offset) {
        return pokeApiClient.listPokemon(limit, offset)
                .flatMap(page -> Flux.fromIterable(page.get("results"))
                        .concatMap(item -> findSummaryByName(item.get("name").asText()))
                        .collectList()
                        .doOnNext(results -> results.forEach(catalogCacheRepository::saveSummary))
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
                            .doOnNext(results -> results.forEach(catalogCacheRepository::saveSummary))
                            .map(results -> new PokemonPage(all.size(), limit, offset, results));
                });
    }

    private Mono<PokemonSummary> findSummaryByName(String name) {
        return fetchPokemon(name)
                .publishOn(Schedulers.boundedElastic())
                .map(summaryMapper::toSummary);
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

package br.com.ruszel.pokedex.infrastructure.pokeapi;

import br.com.ruszel.pokedex.application.usecase.CachePokemonImageUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import br.com.ruszel.pokedex.domain.model.PokemonStat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PokeApiPokemonDetailMapper {
    private final CachePokemonImageUseCase cachePokemonImageUseCase;
    private final PokeApiPokemonSummaryMapper summaryMapper;
    private final PokeApiSpeciesMapper speciesMapper;

    public PokemonDetail toDetail(JsonNode pokemon, JsonNode species, List<String> evolutionChain) {
        Integer id = pokemon.get("id").asInt();
        String imageUrl = cachePokemonImageUseCase.execute(id, "official-artwork", summaryMapper.officialArtwork(pokemon));
        String spriteUrl = cachePokemonImageUseCase.execute(id, "front-default", pokemon.path("sprites").path("front_default").asText(null));
        return new PokemonDetail(
                id,
                pokemon.get("name").asText(),
                imageUrl,
                spriteUrl,
                pokemon.get("height").asInt(),
                pokemon.get("weight").asInt(),
                summaryMapper.extractTypes(pokemon),
                extractAbilities(pokemon),
                extractStats(pokemon),
                speciesMapper.toSpecies(species),
                evolutionChain
        );
    }

    public List<String> extractAbilities(JsonNode pokemon) {
        var abilities = new ArrayList<String>();
        pokemon.path("abilities").forEach(ability -> abilities.add(ability.path("ability").path("name").asText()));
        return abilities;
    }

    public List<PokemonStat> extractStats(JsonNode pokemon) {
        var stats = new ArrayList<PokemonStat>();
        pokemon.path("stats").forEach(stat -> stats.add(new PokemonStat(
                stat.path("stat").path("name").asText(),
                stat.path("base_stat").asInt()
        )));
        return stats;
    }
}

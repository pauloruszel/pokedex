package br.com.ruszel.pokedex.infrastructure.pokeapi;

import br.com.ruszel.pokedex.application.usecase.CachePokemonImageUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PokeApiPokemonSummaryMapper {
    private final CachePokemonImageUseCase cachePokemonImageUseCase;

    public PokemonSummary toSummary(JsonNode pokemon) {
        Integer id = pokemon.get("id").asInt();
        String imageUrl = cachePokemonImageUseCase.execute(id, "official-artwork", officialArtwork(pokemon));
        return new PokemonSummary(
                id,
                pokemon.get("name").asText(),
                imageUrl,
                extractTypes(pokemon)
        );
    }

    public List<String> extractTypes(JsonNode pokemon) {
        var types = new ArrayList<String>();
        pokemon.path("types").forEach(type -> types.add(type.path("type").path("name").asText()));
        return types;
    }

    public String officialArtwork(JsonNode pokemon) {
        return pokemon.path("sprites")
                .path("other")
                .path("official-artwork")
                .path("front_default")
                .asText(null);
    }
}

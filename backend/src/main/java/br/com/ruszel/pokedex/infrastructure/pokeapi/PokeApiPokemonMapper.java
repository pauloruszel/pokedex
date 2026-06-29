package br.com.ruszel.pokedex.infrastructure.pokeapi;

import br.com.ruszel.pokedex.application.usecase.CachePokemonImageUseCase;
import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import br.com.ruszel.pokedex.domain.model.PokemonSpecies;
import br.com.ruszel.pokedex.domain.model.PokemonStat;
import br.com.ruszel.pokedex.domain.model.PokemonSummary;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PokeApiPokemonMapper {
    private static final List<String> SPECIES_TEXT_LANGUAGES = List.of("pt-BR", "pt", "en");

    private final CachePokemonImageUseCase cachePokemonImageUseCase;
    private final SpeciesTextLocalizer speciesTextLocalizer;

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

    public PokemonDetail toDetail(JsonNode pokemon, JsonNode species, List<String> evolutionChain) {
        Integer id = pokemon.get("id").asInt();
        String imageUrl = cachePokemonImageUseCase.execute(id, "official-artwork", officialArtwork(pokemon));
        String spriteUrl = cachePokemonImageUseCase.execute(id, "front-default", pokemon.path("sprites").path("front_default").asText(null));
        return new PokemonDetail(
                id,
                pokemon.get("name").asText(),
                imageUrl,
                spriteUrl,
                pokemon.get("height").asInt(),
                pokemon.get("weight").asInt(),
                extractTypes(pokemon),
                extractAbilities(pokemon),
                extractStats(pokemon),
                toSpecies(species),
                evolutionChain
        );
    }

    private PokemonSpecies toSpecies(JsonNode species) {
        if (species == null || species.isNull()) {
            return new PokemonSpecies(null, null, null, null, null);
        }
        return new PokemonSpecies(
                speciesTextLocalizer.localizeGenus(findLocalizedValue(species.path("genera"), "genus", SPECIES_TEXT_LANGUAGES)),
                speciesTextLocalizer.localizeFlavorText(cleanFlavorText(findLocalizedValue(species.path("flavor_text_entries"), "flavor_text", SPECIES_TEXT_LANGUAGES))),
                textOrNull(species.path("color").path("name")),
                textOrNull(species.path("habitat").path("name")),
                textOrNull(species.path("generation").path("name"))
        );
    }

    private String officialArtwork(JsonNode pokemon) {
        return pokemon.path("sprites")
                .path("other")
                .path("official-artwork")
                .path("front_default")
                .asText(null);
    }

    private List<String> extractTypes(JsonNode pokemon) {
        var types = new ArrayList<String>();
        pokemon.path("types").forEach(type -> types.add(type.path("type").path("name").asText()));
        return types;
    }

    private List<String> extractAbilities(JsonNode pokemon) {
        var abilities = new ArrayList<String>();
        pokemon.path("abilities").forEach(ability -> abilities.add(ability.path("ability").path("name").asText()));
        return abilities;
    }

    private List<PokemonStat> extractStats(JsonNode pokemon) {
        var stats = new ArrayList<PokemonStat>();
        pokemon.path("stats").forEach(stat -> stats.add(new PokemonStat(
                stat.path("stat").path("name").asText(),
                stat.path("base_stat").asInt()
        )));
        return stats;
    }

    private String findLocalizedValue(JsonNode entries, String property, List<String> languages) {
        for (String language : languages) {
            for (JsonNode entry : entries) {
                if (language.equals(entry.path("language").path("name").asText())) {
                    return entry.path(property).asText(null);
                }
            }
        }
        return null;
    }

    private String cleanFlavorText(String text) {
        return Optional.ofNullable(text)
                .map(value -> value.replace("\n", " ").replace("\f", " ").trim())
                .orElse(null);
    }

    private String textOrNull(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }
}

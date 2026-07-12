package br.com.ruszel.pokedex.infrastructure.pokeapi;

import br.com.ruszel.pokedex.domain.model.PokemonSpecies;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PokeApiSpeciesMapper {
    private static final List<String> SPECIES_TEXT_LANGUAGES = List.of("pt-BR", "pt", "en");

    private final SpeciesTextLocalizer speciesTextLocalizer;

    public PokemonSpecies toSpecies(JsonNode species) {
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

    String findLocalizedValue(JsonNode entries, String property, List<String> languages) {
        for (String language : languages) {
            for (JsonNode entry : entries) {
                if (language.equals(entry.path("language").path("name").asText())) {
                    return entry.path(property).asText(null);
                }
            }
        }
        return null;
    }

    String cleanFlavorText(String text) {
        return Optional.ofNullable(text)
                .map(value -> value.replace("\n", " ").replace("\f", " ").trim())
                .orElse(null);
    }

    String textOrNull(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }
}

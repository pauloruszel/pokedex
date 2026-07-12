package br.com.ruszel.pokedex.application.usecase;

import br.com.ruszel.pokedex.domain.model.PokemonSpecies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PokemonSpeciesTranslator {
    static final String SOURCE_LOCALE = "pt-BR";

    private final TranslateTextUseCase translateTextUseCase;

    public PokemonSpecies translate(PokemonSpecies species, Integer pokemonId, String targetLocale) {
        if (species == null || SOURCE_LOCALE.equals(targetLocale)) {
            return species;
        }

        return new PokemonSpecies(
                translate(species.genus(), "pokemon_genus", pokemonId, targetLocale),
                translate(species.flavorText(), "pokemon_flavor_text", pokemonId, targetLocale),
                species.color(),
                species.habitat(),
                species.generation()
        );
    }

    private String translate(String text, String kind, Integer pokemonId, String targetLocale) {
        if (text == null || text.isBlank()) {
            return null;
        }

        TranslateTextUseCase.TranslationResult result = translateTextUseCase.execute(
                new TranslateTextUseCase.TranslationRequest(
                        text,
                        SOURCE_LOCALE,
                        targetLocale,
                        kind,
                        "pokemon",
                        pokemonId == null ? null : pokemonId.toString()
                )
        );

        if ("untranslated".equals(result.source()) || result.text().isBlank()) {
            return null;
        }
        return result.text();
    }
}

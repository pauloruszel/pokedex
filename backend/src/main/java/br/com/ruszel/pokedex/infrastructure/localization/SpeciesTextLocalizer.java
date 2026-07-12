package br.com.ruszel.pokedex.infrastructure.localization;

import br.com.ruszel.pokedex.application.usecase.TranslationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpeciesTextLocalizer {
    public static final String CACHE_LOCALE = "pt-BR-cache-v3";

    private static final Map<String, String> GENUS_TRANSLATIONS = Map.ofEntries(
            Map.entry("Seed Pokémon", "Pokémon Semente"),
            Map.entry("Seed Pokemon", "Pokémon Semente"),
            Map.entry("Lizard Pokémon", "Pokémon Lagarto"),
            Map.entry("Flame Pokémon", "Pokémon Chama"),
            Map.entry("Tiny Turtle Pokémon", "Pokémon Tartaruguinha"),
            Map.entry("Turtle Pokémon", "Pokémon Tartaruga"),
            Map.entry("Shellfish Pokémon", "Pokémon Marisco"),
            Map.entry("Mouse Pokémon", "Pokémon Rato"),
            Map.entry("Poison Bee Pokémon", "Pokémon Abelha Venenosa"),
            Map.entry("Bird Pokémon", "Pokémon Pássaro"),
            Map.entry("Snake Pokémon", "Pokémon Cobra"),
            Map.entry("Cobra Pokémon", "Pokémon Naja"),
            Map.entry("Fox Pokémon", "Pokémon Raposa"),
            Map.entry("Balloon Pokémon", "Pokémon Balão"),
            Map.entry("Bat Pokémon", "Pokémon Morcego"),
            Map.entry("Flower Pokémon", "Pokémon Flor"),
            Map.entry("Mushroom Pokémon", "Pokémon Cogumelo"),
            Map.entry("Duck Pokémon", "Pokémon Pato"),
            Map.entry("Puppy Pokémon", "Pokémon Filhote"),
            Map.entry("Tadpole Pokémon", "Pokémon Girino"),
            Map.entry("Superpower Pokémon", "Pokémon Superpoder"),
            Map.entry("Rock Pokémon", "Pokémon Rocha"),
            Map.entry("Electric Pokémon", "Pokémon Elétrico"),
            Map.entry("Evolution Pokémon", "Pokémon Evolução"),
            Map.entry("Fossil Pokémon", "Pokémon Fóssil"),
            Map.entry("Sleeping Pokémon", "Pokémon Dorminhoco"),
            Map.entry("Genetic Pokémon", "Pokémon Genético"),
            Map.entry("New Species Pokémon", "Pokémon Nova Espécie")
    );

    private static final Map<String, String> FLAVOR_TRANSLATIONS = Map.ofEntries(
            Map.entry(
                    "The plant blooms when it is absorbing solar energy. It stays on the move to seek sunlight.",
                    "A planta floresce quando absorve energia solar. Ele se mantém em movimento em busca da luz do sol."
            ),
            Map.entry(
                    "The flower on its back catches the sun's rays. The sunlight is then absorbed and used for energy.",
                    "A flor em suas costas capta os raios solares. A luz do sol é absorvida e usada como energia."
            ),
            Map.entry(
                    "It raises its tail to check its surroundings. The tail is sometimes struck by lightning in this pose.",
                    "Ele levanta a cauda para observar os arredores. Às vezes, a cauda é atingida por um raio nessa posição."
            )
    );

    private final TranslationCacheService translationCacheService;
    private final PtBrTranslationGateway translationGateway;

    public String localizeGenus(String sourceText) {
        return localize("genus", sourceText, GENUS_TRANSLATIONS);
    }

    public String localizeFlavorText(String sourceText) {
        return localize("flavor_text", sourceText, FLAVOR_TRANSLATIONS);
    }

    private String localize(String kind, String sourceText, Map<String, String> dictionary) {
        if (sourceText == null || sourceText.isBlank()) {
            return sourceText;
        }

        String normalized = normalize(sourceText);
        Optional<TranslationCacheService.CachedTranslation> cached =
                translationCacheService.findLocal(CACHE_LOCALE, kind, normalized);

        if (cached.isPresent()) {
            log.debug("translation cache hit kind={}", kind);
            return cached.get().translatedText();
        }

        String translated = dictionary.get(normalized);
        String source = "local-dictionary";

        if (translated == null) {
            translated = translationGateway.translate(normalized).orElse(null);
            source = "external-provider";
        }

        if (translated == null) {
            log.warn("translation_missing kind={}. Returning null without caching.", kind);
            return null;
        }

        translationCacheService.saveLocal(CACHE_LOCALE, kind, normalized, translated, source);

        log.debug("translation cached kind={} source={}", kind, source);
        return translated;
    }

    private String normalize(String text) {
        return text
                .replace("\n", " ")
                .replace("\f", " ")
                .replace("  ", " ")
                .trim();
    }

}

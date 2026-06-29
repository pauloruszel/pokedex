package br.com.ruszel.pokedex.infrastructure.localization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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

    private final JdbcClient jdbcClient;
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
        String key = key(kind, normalized);

        Optional<String> cached = jdbcClient.sql("""
                        SELECT translated_text
                          FROM pokemon_text_translation
                         WHERE translation_key = :key
                        """)
                .param("key", key)
                .query(String.class)
                .optional();

        if (cached.isPresent()) {
            log.debug("translation cache hit kind={} key={}", kind, key);
            return cached.get();
        }

        String translated = dictionary.get(normalized);
        String source = "local-dictionary";

        if (translated == null) {
            translated = translationGateway.translate(normalized).orElse(null);
            source = "external-provider";
        }

        if (translated == null) {
            log.warn("translation_missing kind={} key={}. Returning null without caching.", kind, key);
            return null;
        }

        jdbcClient.sql("""
                        MERGE INTO pokemon_text_translation (
                            translation_key, source_text, text_kind, locale, translated_text, translation_source, updated_at
                        )
                        KEY(translation_key)
                        VALUES (:key, :sourceText, :kind, :locale, :translatedText, :translationSource, CURRENT_TIMESTAMP)
                        """)
                .param("key", key)
                .param("sourceText", normalized)
                .param("kind", kind)
                .param("locale", CACHE_LOCALE)
                .param("translatedText", translated)
                .param("translationSource", source)
                .update();

        log.debug("translation cached kind={} source={} key={}", kind, source, key);
        return translated;
    }

    private String normalize(String text) {
        return text
                .replace("\n", " ")
                .replace("\f", " ")
                .replace("  ", " ")
                .trim();
    }

    private String fallbackPtBr(String text) {
        return text
                .replace("Pokemon", "Pokémon")
                .replace("POKéMON", "Pokémon")
                .replace("POKÉMON", "Pokémon");
    }

    private String key(String kind, String sourceText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((CACHE_LOCALE + "|" + kind + "|" + sourceText).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for translation cache keys", exception);
        }
    }
}

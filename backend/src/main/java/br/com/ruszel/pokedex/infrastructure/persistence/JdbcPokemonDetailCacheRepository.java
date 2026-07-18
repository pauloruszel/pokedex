package br.com.ruszel.pokedex.infrastructure.persistence;

import br.com.ruszel.pokedex.domain.model.PokemonDetail;
import br.com.ruszel.pokedex.domain.model.PokemonSpecies;
import br.com.ruszel.pokedex.domain.model.PokemonStat;
import br.com.ruszel.pokedex.infrastructure.localization.SpeciesTextLocalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcPokemonDetailCacheRepository {
    private static final String SPECIES_TEXT_CACHE_LOCALE = SpeciesTextLocalizer.CACHE_LOCALE;

    private final JdbcClient jdbcClient;

    public Optional<PokemonDetail> findDetail(String nameOrId) {
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
                        findTypes(rs.getInt("id")),
                        findAbilities(rs.getInt("id")),
                        findStats(rs.getInt("id")),
                        findSpecies(rs.getInt("id")),
                        findEvolutionChain(rs.getInt("id"))
                ))
                .optional();
    }

    @Transactional
    public void saveDetail(PokemonDetail pokemon) {
        int updated = jdbcClient.sql("""
                        UPDATE pokemon
                           SET name = :name,
                               image_url = :imageUrl,
                               sprite_url = :spriteUrl,
                               height = :height,
                               weight = :weight,
                               updated_at = CURRENT_TIMESTAMP
                         WHERE id = :id
                        """)
                .param("id", pokemon.id())
                .param("name", pokemon.name())
                .param("imageUrl", pokemon.imageUrl())
                .param("spriteUrl", pokemon.spriteUrl())
                .param("height", pokemon.height())
                .param("weight", pokemon.weight())
                .update();
        if (updated == 0) {
            jdbcClient.sql("""
                            INSERT INTO pokemon (id, name, image_url, sprite_url, height, weight, updated_at)
                            VALUES (:id, :name, :imageUrl, :spriteUrl, :height, :weight, CURRENT_TIMESTAMP)
                            """)
                    .param("id", pokemon.id())
                    .param("name", pokemon.name())
                    .param("imageUrl", pokemon.imageUrl())
                    .param("spriteUrl", pokemon.spriteUrl())
                    .param("height", pokemon.height())
                    .param("weight", pokemon.weight())
                    .update();
        }

        saveTypes(pokemon.id(), pokemon.types());
        replaceAbilities(pokemon);
        replaceStats(pokemon);
        replaceSpecies(pokemon);
        replaceEvolutionChain(pokemon);
    }

    private List<String> findTypes(int pokemonId) {
        return jdbcClient.sql("SELECT type_name FROM pokemon_type WHERE pokemon_id = :id ORDER BY slot_order")
                .param("id", pokemonId)
                .query(String.class)
                .list();
    }

    private List<String> findAbilities(int pokemonId) {
        return jdbcClient.sql("SELECT ability_name FROM pokemon_ability WHERE pokemon_id = :id ORDER BY slot_order")
                .param("id", pokemonId)
                .query(String.class)
                .list();
    }

    private List<PokemonStat> findStats(int pokemonId) {
        return jdbcClient.sql("SELECT stat_name, stat_value FROM pokemon_stat WHERE pokemon_id = :id")
                .param("id", pokemonId)
                .query((rs, rowNum) -> new PokemonStat(rs.getString("stat_name"), rs.getInt("stat_value")))
                .list();
    }

    private PokemonSpecies findSpecies(int pokemonId) {
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

    private List<String> findEvolutionChain(int pokemonId) {
        return jdbcClient.sql("SELECT evolution_name FROM pokemon_evolution WHERE pokemon_id = :id ORDER BY chain_order")
                .param("id", pokemonId)
                .query(String.class)
                .list();
    }

    private void saveTypes(Integer pokemonId, List<String> types) {
        if (types == null || types.isEmpty()) {
            return;
        }
        int order = 1;
        for (String type : types) {
            saveType(pokemonId, type, order++);
        }
    }

    private void saveType(Integer pokemonId, String type, int order) {
        int updated = jdbcClient.sql("""
                        UPDATE pokemon_type
                           SET slot_order = :order
                         WHERE pokemon_id = :id
                           AND type_name = :type
                        """)
                .param("id", pokemonId)
                .param("type", type)
                .param("order", order)
                .update();
        if (updated == 0) {
            jdbcClient.sql("INSERT INTO pokemon_type (pokemon_id, type_name, slot_order) VALUES (:id, :type, :order)")
                    .param("id", pokemonId)
                    .param("type", type)
                    .param("order", order)
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
        int updated = jdbcClient.sql("""
                        UPDATE pokemon_species
                           SET genus = :genus,
                               flavor_text = :flavorText,
                               text_locale = :textLocale,
                               color = :color,
                               habitat = :habitat,
                               generation = :generation
                         WHERE pokemon_id = :id
                        """)
                .param("id", pokemon.id())
                .param("genus", pokemon.species().genus())
                .param("flavorText", pokemon.species().flavorText())
                .param("textLocale", pokemon.species().flavorText() == null ? null : SPECIES_TEXT_CACHE_LOCALE)
                .param("color", pokemon.species().color())
                .param("habitat", pokemon.species().habitat())
                .param("generation", pokemon.species().generation())
                .update();
        if (updated == 0) {
            jdbcClient.sql("""
                            INSERT INTO pokemon_species (pokemon_id, genus, flavor_text, text_locale, color, habitat, generation)
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
    }

    private void replaceEvolutionChain(PokemonDetail pokemon) {
        jdbcClient.sql("DELETE FROM pokemon_evolution WHERE pokemon_id = :id").param("id", pokemon.id()).update();
        int order = 1;
        for (String evolutionName : pokemon.evolutionChain()) {
            jdbcClient.sql("INSERT INTO pokemon_evolution (pokemon_id, evolution_name, chain_order) VALUES (:id, :name, :order)")
                    .param("id", pokemon.id()).param("name", evolutionName).param("order", order++).update();
        }
    }
}

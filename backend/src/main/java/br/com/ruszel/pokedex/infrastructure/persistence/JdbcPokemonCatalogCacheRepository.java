package br.com.ruszel.pokedex.infrastructure.persistence;

import br.com.ruszel.pokedex.domain.model.PokemonPage;
import br.com.ruszel.pokedex.domain.model.PokemonSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcPokemonCatalogCacheRepository {
    private final JdbcClient jdbcClient;

    public PokemonPage findPage(int limit, int offset) {
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
                        findTypes(rs.getInt("id"))
                ))
                .list();
        return new PokemonPage(count, limit, offset, results);
    }

    public PokemonPage findPageByType(String typeName, int limit, int offset) {
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
                        findTypes(rs.getInt("id"))
                ))
                .list();
        return new PokemonPage(count, limit, offset, results);
    }

    public List<String> findTypes() {
        return jdbcClient.sql("SELECT name FROM pokemon_reference_type ORDER BY display_order")
                .query(String.class)
                .list();
    }

    @Transactional
    public void saveSummary(PokemonSummary pokemon) {
        int updated = jdbcClient.sql("""
                        UPDATE pokemon
                           SET name = :name,
                               image_url = :imageUrl,
                               updated_at = CURRENT_TIMESTAMP
                         WHERE id = :id
                        """)
                .param("id", pokemon.id())
                .param("name", pokemon.name())
                .param("imageUrl", pokemon.imageUrl())
                .update();
        if (updated == 0) {
            jdbcClient.sql("""
                            INSERT INTO pokemon (id, name, image_url, sprite_url, updated_at)
                            VALUES (:id, :name, :imageUrl, NULL, CURRENT_TIMESTAMP)
                            """)
                    .param("id", pokemon.id())
                    .param("name", pokemon.name())
                    .param("imageUrl", pokemon.imageUrl())
                    .update();
        }
        saveTypes(pokemon.id(), pokemon.types());
    }

    private List<String> findTypes(int pokemonId) {
        return jdbcClient.sql("SELECT type_name FROM pokemon_type WHERE pokemon_id = :id ORDER BY slot_order")
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
}

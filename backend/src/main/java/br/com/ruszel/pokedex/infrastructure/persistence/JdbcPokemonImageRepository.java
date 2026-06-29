package br.com.ruszel.pokedex.infrastructure.persistence;

import br.com.ruszel.pokedex.application.port.PokemonImageRepository;
import br.com.ruszel.pokedex.domain.model.PokemonImage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcPokemonImageRepository implements PokemonImageRepository {
    private final JdbcClient jdbcClient;

    @Override
    public Optional<PokemonImage> findByPokemonIdAndType(Integer pokemonId, String imageType) {
        return jdbcClient.sql("""
                        SELECT pokemon_id, image_type, source_url, local_path, public_url, content_type, size_bytes
                          FROM pokemon_image
                         WHERE pokemon_id = :pokemonId
                           AND image_type = :imageType
                        """)
                .param("pokemonId", pokemonId)
                .param("imageType", imageType)
                .query((rs, rowNum) -> new PokemonImage(
                        rs.getInt("pokemon_id"),
                        rs.getString("image_type"),
                        rs.getString("source_url"),
                        rs.getString("local_path"),
                        rs.getString("public_url"),
                        rs.getString("content_type"),
                        rs.getLong("size_bytes")
                ))
                .optional();
    }

    @Override
    public void save(PokemonImage image) {
        jdbcClient.sql("""
                        MERGE INTO pokemon_image (
                            pokemon_id, image_type, source_url, local_path, public_url, content_type, size_bytes, cached_at
                        )
                        KEY(pokemon_id, image_type)
                        VALUES (
                            :pokemonId, :imageType, :sourceUrl, :localPath, :publicUrl, :contentType, :sizeBytes, CURRENT_TIMESTAMP
                        )
                        """)
                .param("pokemonId", image.pokemonId())
                .param("imageType", image.imageType())
                .param("sourceUrl", image.sourceUrl())
                .param("localPath", image.localPath())
                .param("publicUrl", image.publicUrl())
                .param("contentType", image.contentType())
                .param("sizeBytes", image.sizeBytes())
                .update();
    }
}

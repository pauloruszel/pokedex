package br.com.ruszel.pokedex.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Carta calculada para o modo Super Trunfo.")
public record TrunfoCard(
        @Schema(description = "ID nacional do Pokémon.", example = "25")
        Integer id,
        @Schema(description = "Nome da carta.", example = "pikachu")
        String name,
        @Schema(description = "URL interna da imagem da carta.", example = "/api/pokemon/25/images/official-artwork")
        String imageUrl,
        @Schema(description = "Tipos usados para identidade visual e filtros.", example = "[\"electric\"]")
        List<String> types,
        @Schema(description = "Raridade calculada para balanceamento.", example = "rare")
        String rarity,
        @Schema(description = "Indica se a carta recebeu bônus especial de lendário/mítico.", example = "false")
        Boolean legendaryCharge,
        @Schema(description = "Atributos numéricos usados nas rodadas.")
        TrunfoAttributes attributes
) {
}

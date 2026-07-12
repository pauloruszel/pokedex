package br.com.ruszel.pokedex.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Atributos de batalha usados pelo modo Super Trunfo.")
public record TrunfoAttributes(
        @Schema(description = "HP base.", example = "35")
        Integer hp,
        @Schema(description = "Ataque base.", example = "55")
        Integer attack,
        @Schema(description = "Defesa base.", example = "40")
        Integer defense,
        @Schema(description = "Ataque especial base.", example = "50")
        Integer specialAttack,
        @Schema(description = "Defesa especial base.", example = "50")
        Integer specialDefense,
        @Schema(description = "Velocidade base.", example = "90")
        Integer speed,
        @Schema(description = "Peso convertido para quilogramas.", example = "6.0")
        Double weight,
        @Schema(description = "Altura convertida para metros.", example = "0.4")
        Double height,
        @Schema(description = "Soma dos status principais.", example = "320")
        Integer total
) {
}

package br.com.ruszel.pokedex.domain.model;

public record TrunfoAttributes(
        Integer hp,
        Integer attack,
        Integer defense,
        Integer specialAttack,
        Integer specialDefense,
        Integer speed,
        Double weight,
        Double height,
        Integer total
) {
}

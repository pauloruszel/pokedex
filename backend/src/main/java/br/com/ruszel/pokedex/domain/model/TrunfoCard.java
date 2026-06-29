package br.com.ruszel.pokedex.domain.model;

import java.util.List;

public record TrunfoCard(
        Integer id,
        String name,
        String imageUrl,
        List<String> types,
        String rarity,
        Boolean legendaryCharge,
        TrunfoAttributes attributes
) {
}

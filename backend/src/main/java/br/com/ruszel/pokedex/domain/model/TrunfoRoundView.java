package br.com.ruszel.pokedex.domain.model;

public record TrunfoRoundView(
        Integer round,
        String attribute,
        TrunfoCard playerOneCard,
        TrunfoCard playerTwoCard,
        Number playerOneValue,
        Number playerTwoValue,
        String result,
        Integer potSize
) {
}

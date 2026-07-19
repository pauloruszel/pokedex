package br.com.ruszel.pokedex.domain.model;

import java.util.List;

public record TrunfoRoomView(
        String code,
        String state,
        String mode,
        String difficulty,
        String type,
        String deckSelection,
        Integer deckSize,
        String playerSide,
        String playerToken,
        String playerOneName,
        String playerTwoName,
        String currentTurn,
        Integer round,
        Integer playerDeckCount,
        Integer opponentDeckCount,
        Integer disputePileCount,
        TrunfoCard playerCard,
        TrunfoCard opponentCard,
        TrunfoRoundView lastRound,
        String winner,
        List<TrunfoRoundView> history
) {
}

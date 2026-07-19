export type TurnSide = 'player-one' | 'player-two';
export type TurnRoundResult = 'player' | 'cpu' | 'draw';

export function nextTurnForResult(result: TurnRoundResult, currentTurn: TurnSide): TurnSide {
  if (result === 'player') return 'player-one';
  if (result === 'cpu') return 'player-two';
  return currentTurn;
}

export function localWinnerLabel(playerOneCards: number, playerTwoCards: number): string | null {
  if (playerOneCards === 0 && playerTwoCards === 0) return 'Empate';
  if (playerOneCards === 0) return 'Jogador 2';
  if (playerTwoCards === 0) return 'Jogador 1';
  return null;
}

export function applyRoundResult<T>(
  result: TurnRoundResult,
  playerOneDeck: T[],
  playerTwoDeck: T[],
  disputePile: T[]
) {
  const playerOneRest = playerOneDeck.slice(1);
  const playerTwoRest = playerTwoDeck.slice(1);
  const stake = [...disputePile, playerOneDeck[0], playerTwoDeck[0]].filter((card): card is T => card !== undefined);

  if (result === 'player') {
    return { playerOneDeck: [...playerOneRest, ...stake], playerTwoDeck: playerTwoRest, disputePile: [] };
  }

  if (result === 'cpu') {
    return { playerOneDeck: playerOneRest, playerTwoDeck: [...playerTwoRest, ...stake], disputePile: [] };
  }

  return { playerOneDeck: playerOneRest, playerTwoDeck: playerTwoRest, disputePile: stake };
}

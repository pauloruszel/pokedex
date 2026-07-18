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

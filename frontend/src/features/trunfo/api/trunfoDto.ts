import type { TrunfoAttributeKey, TrunfoRarity } from '../types/trunfoCard';

export type TrunfoApiCard = {
  id: number;
  name: string;
  imageUrl: string;
  types: string[];
  rarity: TrunfoRarity;
  legendaryCharge: boolean;
  attributes: Record<TrunfoAttributeKey, number>;
};

export type TrunfoRoomRoundDto = {
  round: number;
  attribute: TrunfoAttributeKey;
  playerOneCard: TrunfoApiCard;
  playerTwoCard: TrunfoApiCard;
  playerOneValue: number;
  playerTwoValue: number;
  result: 'player' | 'cpu' | 'draw';
  potSize: number;
};

export type TrunfoRoomDto = {
  code: string;
  state: 'WAITING_FOR_PLAYER' | 'DECK_SELECTION' | 'IN_PROGRESS' | 'FINISHED';
  mode: string;
  difficulty: 'casual' | 'balanced' | 'expert';
  type: string | null;
  deckSelection: 'auto' | 'manual';
  deckSize: number;
  playerSide: 'player-one' | 'player-two';
  playerToken: string;
  playerOneName: string;
  playerTwoName: string | null;
  currentTurn: 'player-one' | 'player-two';
  round: number;
  playerDeckCount: number;
  opponentDeckCount: number;
  disputePileCount: number;
  playerCard: TrunfoApiCard | null;
  opponentCard: TrunfoApiCard | null;
  lastRound: TrunfoRoomRoundDto | null;
  winner: string | null;
  history: TrunfoRoomRoundDto[];
};

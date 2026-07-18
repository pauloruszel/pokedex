import type { TrunfoAttributeKey } from './trunfoCard';

export type TrunfoDifficulty = 'casual' | 'balanced' | 'expert';
export type TrunfoMode = 'all' | 'favorites' | 'type';
export type TrunfoDeckSelection = 'auto' | 'manual';
export type TrunfoGameMode = 'cpu' | 'local-pvp';
export type PlayerSide = 'player-one' | 'player-two';
export type RoundResult = 'player' | 'cpu' | 'draw';
export type GameStatus = 'setup' | 'loading' | 'ready' | 'revealed' | 'finished';

export type RoundHistoryItem = {
  round: number;
  playerName: string;
  cpuName: string;
  attribute: TrunfoAttributeKey;
  playerValue: number;
  cpuValue: number;
  result: RoundResult;
  potSize: number;
};

export type TrunfoSetup = {
  mode: TrunfoMode;
  deckSelection: TrunfoDeckSelection;
  gameMode: TrunfoGameMode;
  type: string;
  difficulty: TrunfoDifficulty;
};

import type { TrunfoAttributeKey } from './trunfoCard';

export type TrunfoDifficulty = 'casual' | 'balanced' | 'expert';
export type TrunfoMode = 'all' | 'favorites' | 'type';
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
  type: string;
  difficulty: TrunfoDifficulty;
};

import type { PokemonDetail, PokemonSummary } from '../../pokemon/types/pokemon';

export type TrunfoAttributeKey =
  | 'hp'
  | 'attack'
  | 'defense'
  | 'specialAttack'
  | 'specialDefense'
  | 'speed'
  | 'weight'
  | 'height'
  | 'total';

export type TrunfoDifficulty = 'casual' | 'balanced' | 'expert';
export type TrunfoMode = 'all' | 'favorites' | 'type';
export type TrunfoRarity = 'comum' | 'rara' | 'epica' | 'lendaria';
export type RoundResult = 'player' | 'cpu' | 'draw';
export type GameStatus = 'setup' | 'loading' | 'ready' | 'revealed' | 'finished';

export type TrunfoCardModel = {
  id: number;
  summary: PokemonSummary;
  detail: PokemonDetail;
  rarity: TrunfoRarity;
  attributes: Record<TrunfoAttributeKey, number>;
  legendaryCharge: boolean;
};

export type RoundHistoryItem = {
  round: number;
  playerName: string;
  cpuName: string;
  attribute: TrunfoAttributeKey;
  playerValue: number;
  cpuValue: number;
  result: RoundResult;
};

export type TrunfoSetup = {
  mode: TrunfoMode;
  type: string;
  difficulty: TrunfoDifficulty;
};

export type AttributeOption = {
  key: TrunfoAttributeKey;
  label: string;
  shortLabel: string;
  unit?: string;
};

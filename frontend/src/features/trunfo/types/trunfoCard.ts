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

export type TrunfoRarity = 'comum' | 'rara' | 'epica' | 'lendaria';

export type TrunfoCardModel = {
  id: number;
  summary: PokemonSummary;
  detail: PokemonDetail;
  rarity: TrunfoRarity;
  attributes: Record<TrunfoAttributeKey, number>;
  legendaryCharge: boolean;
};

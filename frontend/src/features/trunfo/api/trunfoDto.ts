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

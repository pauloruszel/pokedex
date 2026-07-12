import type { TrunfoAttributeKey, TrunfoCardModel, TrunfoRarity } from '../types/trunfoCard';
import type { RoundResult, TrunfoDifficulty } from '../types/trunfoGame';
import { ATTRIBUTE_OPTIONS } from './trunfoAttributes';

export function compareCards(
  player: TrunfoCardModel,
  cpu: TrunfoCardModel,
  attribute: TrunfoAttributeKey
): RoundResult {
  const playerValue = player.attributes[attribute];
  const cpuValue = cpu.attributes[attribute];

  if (playerValue > cpuValue) return 'player';
  if (cpuValue > playerValue) return 'cpu';

  if (player.legendaryCharge && !cpu.legendaryCharge) return 'player';
  if (cpu.legendaryCharge && !player.legendaryCharge) return 'cpu';

  return 'draw';
}

export function chooseCpuAttribute(card: TrunfoCardModel, difficulty: TrunfoDifficulty): TrunfoAttributeKey {
  const ranked = ATTRIBUTE_OPTIONS
    .map((option) => ({ key: option.key, value: card.attributes[option.key] }))
    .sort((a, b) => b.value - a.value);

  if (difficulty === 'expert') return ranked[0].key;
  if (difficulty === 'balanced') return ranked[Math.floor(Math.random() * Math.min(3, ranked.length))].key;

  return ranked[Math.floor(Math.random() * ranked.length)].key;
}

export function getRarity(total: number): TrunfoRarity {
  if (total >= 581) return 'lendaria';
  if (total >= 481) return 'epica';
  if (total >= 351) return 'rara';
  return 'comum';
}

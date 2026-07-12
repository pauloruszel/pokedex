import type { PokemonDetail, PokemonStat, PokemonSummary } from '../../pokemon/types/pokemon';
import type { TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfoCard';
import { getRarity } from './trunfoRules';

const STAT_ALIASES: Record<TrunfoAttributeKey, string[]> = {
  hp: ['hp'],
  attack: ['attack', 'ataque'],
  defense: ['defense', 'defesa'],
  specialAttack: ['special-attack', 'special attack', 'ataque especial'],
  specialDefense: ['special-defense', 'special defense', 'defesa especial'],
  speed: ['speed', 'velocidade'],
  weight: [],
  height: [],
  total: []
};

export function createTrunfoCard(summary: PokemonSummary, detail: PokemonDetail): TrunfoCardModel {
  const attributes = extractAttributes(detail);

  return {
    id: summary.id,
    summary,
    detail,
    attributes,
    rarity: getRarity(attributes.total),
    legendaryCharge: attributes.total >= 581
  };
}

function extractAttributes(detail: PokemonDetail): Record<TrunfoAttributeKey, number> {
  const hp = findStat(detail.stats, 'hp');
  const attack = findStat(detail.stats, 'attack');
  const defense = findStat(detail.stats, 'defense');
  const specialAttack = findStat(detail.stats, 'specialAttack');
  const specialDefense = findStat(detail.stats, 'specialDefense');
  const speed = findStat(detail.stats, 'speed');
  const total = hp + attack + defense + specialAttack + specialDefense + speed;

  return {
    hp,
    attack,
    defense,
    specialAttack,
    specialDefense,
    speed,
    weight: Math.round(detail.weight * 10) / 100,
    height: Math.round(detail.height * 10) / 100,
    total
  };
}

function findStat(stats: PokemonStat[], attribute: TrunfoAttributeKey) {
  const aliases = STAT_ALIASES[attribute];
  const stat = stats.find((item) => aliases.includes(item.name.toLowerCase().trim()));

  return stat?.value ?? 0;
}

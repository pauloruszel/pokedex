import type { PokemonDetail, PokemonStat, PokemonSummary } from '../../pokemon/types/pokemon';
import type {
  AttributeOption,
  RoundResult,
  TrunfoAttributeKey,
  TrunfoCardModel,
  TrunfoDifficulty,
  TrunfoRarity
} from '../types/trunfo';

export const ATTRIBUTE_OPTIONS: AttributeOption[] = [
  { key: 'hp', label: 'HP', shortLabel: 'HP' },
  { key: 'attack', label: 'Ataque', shortLabel: 'ATQ' },
  { key: 'defense', label: 'Defesa', shortLabel: 'DEF' },
  { key: 'specialAttack', label: 'Ataque especial', shortLabel: 'ATQ ESP' },
  { key: 'specialDefense', label: 'Defesa especial', shortLabel: 'DEF ESP' },
  { key: 'speed', label: 'Velocidade', shortLabel: 'VEL' },
  { key: 'weight', label: 'Peso', shortLabel: 'PESO', unit: 'kg' },
  { key: 'height', label: 'Altura', shortLabel: 'ALT', unit: 'm' },
  { key: 'total', label: 'Total de status', shortLabel: 'TOTAL' }
];

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

export function extractAttributes(detail: PokemonDetail): Record<TrunfoAttributeKey, number> {
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

export function formatAttributeValue(attribute: TrunfoAttributeKey, value: number) {
  const option = ATTRIBUTE_OPTIONS.find((item) => item.key === attribute);

  if (attribute === 'height') return `${value.toFixed(1)} m`;
  if (attribute === 'weight') return `${value.toFixed(1)} kg`;

  return option?.unit ? `${value} ${option.unit}` : String(value);
}

export function getAttributeLabel(attribute: TrunfoAttributeKey) {
  return ATTRIBUTE_OPTIONS.find((item) => item.key === attribute)?.label ?? attribute;
}

export function chooseCpuAttribute(card: TrunfoCardModel, difficulty: TrunfoDifficulty): TrunfoAttributeKey {
  const ranked = ATTRIBUTE_OPTIONS
    .map((option) => ({ key: option.key, value: card.attributes[option.key] }))
    .sort((a, b) => b.value - a.value);

  if (difficulty === 'expert') return ranked[0].key;
  if (difficulty === 'balanced') return ranked[Math.floor(Math.random() * Math.min(3, ranked.length))].key;

  return ranked[Math.floor(Math.random() * ranked.length)].key;
}

export function buildDeckPool(pokemons: PokemonSummary[], targetSize: number) {
  return shuffle([...pokemons]).slice(0, Math.max(targetSize, 2));
}

export function splitDeck(cards: TrunfoCardModel[]) {
  const shuffled = shuffle(cards);
  const midpoint = Math.floor(shuffled.length / 2);

  return {
    playerDeck: shuffled.slice(0, midpoint),
    cpuDeck: shuffled.slice(midpoint, midpoint * 2)
  };
}

export function getRarity(total: number): TrunfoRarity {
  if (total >= 581) return 'lendaria';
  if (total >= 481) return 'epica';
  if (total >= 351) return 'rara';
  return 'comum';
}

export function getRarityLabel(rarity: TrunfoRarity) {
  const labels: Record<TrunfoRarity, string> = {
    comum: 'Comum',
    rara: 'Rara',
    epica: 'Épica',
    lendaria: 'Lendária'
  };

  return labels[rarity];
}

function findStat(stats: PokemonStat[], attribute: TrunfoAttributeKey) {
  const aliases = STAT_ALIASES[attribute];
  const stat = stats.find((item) => aliases.includes(normalize(item.name)));

  return stat?.value ?? 0;
}

function normalize(value: string) {
  return value.toLowerCase().trim();
}

function shuffle<T>(items: T[]) {
  const copy = [...items];

  for (let index = copy.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(Math.random() * (index + 1));
    [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
  }

  return copy;
}

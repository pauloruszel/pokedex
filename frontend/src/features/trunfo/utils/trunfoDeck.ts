import type { PokemonSummary } from '../../pokemon/types/pokemon';
import type { TrunfoCardModel } from '../types/trunfoCard';

export const TRUNFO_DECK_SIZE = 20;

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

export function buildBalancedCpuDeck(cards: TrunfoCardModel[], playerDeck: TrunfoCardModel[]) {
  const selectedIds = new Set(playerDeck.map((card) => card.id));
  const available = cards.filter((card) => !selectedIds.has(card.id));
  const cpuDeck: TrunfoCardModel[] = [];

  for (const playerCard of playerDeck) {
    const match = closestCpuCard(playerCard, available, cpuDeck);
    if (match) cpuDeck.push(match);
  }

  return shuffle(cpuDeck);
}

function closestCpuCard(playerCard: TrunfoCardModel, available: TrunfoCardModel[], selected: TrunfoCardModel[]) {
  const selectedIds = new Set(selected.map((card) => card.id));

  return available
    .filter((card) => !selectedIds.has(card.id))
    .sort((a, b) => scoreMatch(playerCard, a) - scoreMatch(playerCard, b))[0] ?? null;
}

function scoreMatch(playerCard: TrunfoCardModel, cpuCard: TrunfoCardModel) {
  const rarityPenalty = playerCard.rarity === cpuCard.rarity ? 0 : 90;
  return Math.abs(playerCard.attributes.total - cpuCard.attributes.total) + rarityPenalty;
}

function shuffle<T>(items: T[]) {
  const copy = [...items];

  for (let index = copy.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(Math.random() * (index + 1));
    [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
  }

  return copy;
}

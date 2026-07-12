import type { PokemonSummary } from '../../pokemon/types/pokemon';
import type { TrunfoCardModel } from '../types/trunfoCard';

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

function shuffle<T>(items: T[]) {
  const copy = [...items];

  for (let index = copy.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(Math.random() * (index + 1));
    [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
  }

  return copy;
}

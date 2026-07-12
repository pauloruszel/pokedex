import type { PokemonSummary } from '../../pokemon/types/pokemon';
import type { TrunfoCardModel } from '../types/trunfoCard';
import type { TrunfoApiCard } from './trunfoDto';

export function createTrunfoCardFromApi(card: TrunfoApiCard): TrunfoCardModel {
  const summary: PokemonSummary = {
    id: card.id,
    name: card.name,
    imageUrl: card.imageUrl,
    types: card.types
  };

  return {
    id: card.id,
    summary,
    detail: {
      ...summary,
      spriteUrl: null,
      height: card.attributes.height * 10,
      weight: card.attributes.weight * 10,
      abilities: [],
      stats: [
        { name: 'hp', value: card.attributes.hp },
        { name: 'attack', value: card.attributes.attack },
        { name: 'defense', value: card.attributes.defense },
        { name: 'special-attack', value: card.attributes.specialAttack },
        { name: 'special-defense', value: card.attributes.specialDefense },
        { name: 'speed', value: card.attributes.speed }
      ],
      species: {},
      evolutionChain: []
    },
    attributes: card.attributes,
    rarity: card.rarity,
    legendaryCharge: card.legendaryCharge
  };
}

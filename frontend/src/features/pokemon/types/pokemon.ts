export type PokemonStat = {
  name: string;
  value: number;
};

export type PokemonSpecies = {
  genus?: string | null;
  flavorText?: string | null;
  color?: string | null;
  habitat?: string | null;
  generation?: string | null;
};

export type PokemonSummary = {
  id: number;
  name: string;
  imageUrl: string;
  types: string[];
};

export type PokemonDetail = PokemonSummary & {
  spriteUrl?: string | null;
  height: number;
  weight: number;
  abilities: string[];
  stats: PokemonStat[];
  species: PokemonSpecies;
  evolutionChain: string[];
};

export type PokemonPage = {
  count: number;
  limit: number;
  offset: number;
  results: PokemonSummary[];
};

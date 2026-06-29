import { API_BASE_URL } from '../../../shared/utils/assets';
import type { PokemonDetail, PokemonPage } from '../types/pokemon';

async function request<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { signal });

  if (!response.ok) {
    throw new Error(`Erro ao consultar API: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export const pokemonApi = {
  list: (limit = 24, offset = 0) => request<PokemonPage>(`/api/pokemon?limit=${limit}&offset=${offset}`),
  detail: (nameOrId: string | number, signal?: AbortSignal) => request<PokemonDetail>(`/api/pokemon/${nameOrId}`, signal),
  search: (query: string) => request<PokemonDetail>(`/api/pokemon/search?q=${encodeURIComponent(query)}`),
  types: () => request<string[]>('/api/pokemon/types'),
  byType: (typeName: string, limit = 24, offset = 0) =>
    request<PokemonPage>(`/api/pokemon/type/${typeName}?limit=${limit}&offset=${offset}`)
};

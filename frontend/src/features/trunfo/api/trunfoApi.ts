import { API_BASE_URL } from '../../../shared/api/apiConfig';
import type { TrunfoDifficulty } from '../types/trunfoGame';
import type { TrunfoApiCard } from './trunfoDto';

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`);

  if (!response.ok) {
    throw new Error(`Erro ao consultar API: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export const trunfoApi = {
  cards: (limit = 40, mode: TrunfoDifficulty = 'balanced', type?: string) => {
    const params = new URLSearchParams({
      limit: String(limit),
      mode
    });

    if (type) {
      params.set('type', type);
    }

    return request<TrunfoApiCard[]>(`/api/trunfo/cards?${params.toString()}`);
  }
};

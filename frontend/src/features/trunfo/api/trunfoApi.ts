import { API_BASE_URL } from '../../../shared/api/apiConfig';
import type { TrunfoDifficulty } from '../types/trunfoGame';
import type { TrunfoAttributeKey } from '../types/trunfoCard';
import type { TrunfoApiCard, TrunfoRoomDto } from './trunfoDto';
import { readApiErrorMessage } from './apiError';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, init);

  if (!response.ok) {
    throw new Error(await readApiErrorMessage(response, `Erro ao consultar API: ${response.status}`));
  }

  return response.json() as Promise<T>;
}

export const trunfoApi = {
  cards: (limit = 40, mode: TrunfoDifficulty = 'balanced', type?: string, offset = 0) => {
    const params = new URLSearchParams({
      limit: String(limit),
      mode,
      offset: String(offset)
    });

    if (type) {
      params.set('type', type);
    }

    return request<TrunfoApiCard[]>(`/api/trunfo/cards?${params.toString()}`);
  },
  createRoom: (body: { nickname: string; mode: string; difficulty: TrunfoDifficulty; type?: string; deckSelection?: string; deckSize?: number }) => (
    request<TrunfoRoomDto>('/api/trunfo/rooms', jsonRequest(body))
  ),
  joinRoom: (code: string, nickname: string) => (
    request<TrunfoRoomDto>(`/api/trunfo/rooms/${encodeURIComponent(code)}/join`, jsonRequest({ nickname }))
  ),
  room: (code: string, playerToken: string) => (
    request<TrunfoRoomDto>(`/api/trunfo/rooms/${encodeURIComponent(code)}?${new URLSearchParams({ playerToken })}`)
  ),
  playRoomRound: (code: string, playerToken: string, attribute: TrunfoAttributeKey) => (
    request<TrunfoRoomDto>(`/api/trunfo/rooms/${encodeURIComponent(code)}/rounds`, jsonRequest({ playerToken, attribute }))
  ),
  leaveRoom: (code: string, playerToken: string) => (
    request<TrunfoRoomDto>(`/api/trunfo/rooms/${encodeURIComponent(code)}/leave`, jsonRequest({ playerToken }))
  ),
  confirmDeck: (code: string, playerToken: string, cardIds: number[]) => (
    request<TrunfoRoomDto>(`/api/trunfo/rooms/${encodeURIComponent(code)}/deck`, jsonRequest({ playerToken, cardIds }))
  )
};

function jsonRequest(body: unknown): RequestInit {
  return {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  }
}

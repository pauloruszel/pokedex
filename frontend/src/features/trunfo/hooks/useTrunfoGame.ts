import { useMemo, useState } from 'react';
import type { PokemonDetail, PokemonSummary } from '../../pokemon/types/pokemon';
import {
  buildDeckPool,
  chooseCpuAttribute,
  compareCards,
  createTrunfoCard,
  splitDeck
} from '../utils/trunfoRules';
import type {
  GameStatus,
  RoundHistoryItem,
  TrunfoAttributeKey,
  TrunfoCardModel,
  TrunfoDifficulty
} from '../types/trunfo';

type StartGameParams = {
  candidates: PokemonSummary[];
  difficulty: TrunfoDifficulty;
  loadDetail: (pokemon: PokemonSummary) => Promise<PokemonDetail>;
};

export function useTrunfoGame() {
  const [status, setStatus] = useState<GameStatus>('setup');
  const [playerDeck, setPlayerDeck] = useState<TrunfoCardModel[]>([]);
  const [cpuDeck, setCpuDeck] = useState<TrunfoCardModel[]>([]);
  const [disputePile, setDisputePile] = useState<TrunfoCardModel[]>([]);
  const [history, setHistory] = useState<RoundHistoryItem[]>([]);
  const [selectedAttribute, setSelectedAttribute] = useState<TrunfoAttributeKey | null>(null);
  const [roundResult, setRoundResult] = useState<RoundHistoryItem | null>(null);
  const [difficulty, setDifficulty] = useState<TrunfoDifficulty>('balanced');
  const [error, setError] = useState<string | null>(null);

  const playerCard = playerDeck[0] ?? null;
  const cpuCard = cpuDeck[0] ?? null;
  const round = history.length + 1;

  const winner = useMemo(() => {
    if (playerDeck.length === 0 && cpuDeck.length === 0) return 'Empate';
    if (playerDeck.length === 0 && cpuDeck.length > 0) return 'CPU';
    if (cpuDeck.length === 0 && playerDeck.length > 0) return 'Você';
    return null;
  }, [cpuDeck.length, playerDeck.length]);

  async function startGame({ candidates, difficulty: nextDifficulty, loadDetail }: StartGameParams) {
    setStatus('loading');
    setError(null);
    setHistory([]);
    setDisputePile([]);
    setRoundResult(null);
    setSelectedAttribute(null);
    setDifficulty(nextDifficulty);

    try {
      const pool = buildDeckPool(candidates, 40);

      if (pool.length < 4) {
        throw new Error('Poucos Pokémon disponíveis para montar uma partida.');
      }

      const details = await Promise.all(pool.map(async (pokemon) => createTrunfoCard(pokemon, await loadDetail(pokemon))));
      const { playerDeck: nextPlayerDeck, cpuDeck: nextCpuDeck } = splitDeck(details);

      setPlayerDeck(nextPlayerDeck);
      setCpuDeck(nextCpuDeck);
      setStatus('ready');
    } catch (exception) {
      setStatus('setup');
      setError(exception instanceof Error ? exception.message : 'Não foi possível iniciar a partida.');
    }
  }

  function playRound(attribute: TrunfoAttributeKey) {
    if (!playerCard || !cpuCard || status !== 'ready') {
      return;
    }

    const result = compareCards(playerCard, cpuCard, attribute);
    const item: RoundHistoryItem = {
      round,
      attribute,
      playerName: playerCard.summary.name,
      cpuName: cpuCard.summary.name,
      playerValue: playerCard.attributes[attribute],
      cpuValue: cpuCard.attributes[attribute],
      result,
      potSize: disputePile.length + 2
    };

    setSelectedAttribute(attribute);
    setRoundResult(item);
    setHistory((current) => [item, ...current].slice(0, 12));
    setStatus('revealed');
  }

  function nextRound() {
    if (!playerCard || !cpuCard || !roundResult) {
      return;
    }

    const playerRest = playerDeck.slice(1);
    const cpuRest = cpuDeck.slice(1);
    const stake = [...disputePile, playerCard, cpuCard];

    if (roundResult.result === 'player') {
      setPlayerDeck([...playerRest, ...stake]);
      setCpuDeck(cpuRest);
      setDisputePile([]);
    } else if (roundResult.result === 'cpu') {
      setPlayerDeck(playerRest);
      setCpuDeck([...cpuRest, ...stake]);
      setDisputePile([]);
    } else {
      setPlayerDeck(playerRest);
      setCpuDeck(cpuRest);
      setDisputePile(stake);
    }

    setSelectedAttribute(null);
    setRoundResult(null);

    const nextPlayerCount = roundResult.result === 'player'
      ? playerRest.length + stake.length
      : playerRest.length;
    const nextCpuCount = roundResult.result === 'cpu'
      ? cpuRest.length + stake.length
      : cpuRest.length;
    setStatus(nextPlayerCount === 0 || nextCpuCount === 0 ? 'finished' : 'ready');
  }

  function cpuSuggestion() {
    return cpuCard ? chooseCpuAttribute(cpuCard, difficulty) : null;
  }

  function resetGame() {
    setStatus('setup');
    setPlayerDeck([]);
    setCpuDeck([]);
    setDisputePile([]);
    setHistory([]);
    setSelectedAttribute(null);
    setRoundResult(null);
    setError(null);
  }

  return {
    status,
    playerDeck,
    cpuDeck,
    disputePile,
    playerCard,
    cpuCard,
    history,
    selectedAttribute,
    roundResult,
    round,
    winner,
    error,
    startGame,
    playRound,
    nextRound,
    resetGame,
    cpuSuggestion
  };
}

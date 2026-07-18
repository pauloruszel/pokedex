import { useMemo, useState } from 'react';
import type { PokemonDetail, PokemonSummary } from '../../pokemon/types/pokemon';
import { buildDeckPool, splitDeck } from '../utils/trunfoDeck';
import { createTrunfoCard } from '../utils/trunfoCardFactory';
import { chooseCpuAttribute, compareCards } from '../utils/trunfoRules';
import { applyRoundResult, localWinnerLabel, nextTurnForResult } from '../utils/trunfoTurnRules';
import type { TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfoCard';
import type {
  GameStatus,
  PlayerSide,
  RoundHistoryItem,
  TrunfoDifficulty,
  TrunfoGameMode
} from '../types/trunfoGame';

type StartGameParams = {
  candidates?: PokemonSummary[];
  cards?: TrunfoCardModel[];
  playerCards?: TrunfoCardModel[];
  cpuCards?: TrunfoCardModel[];
  difficulty: TrunfoDifficulty;
  gameMode?: TrunfoGameMode;
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
  const [gameMode, setGameMode] = useState<TrunfoGameMode>('cpu');
  const [currentTurn, setCurrentTurn] = useState<PlayerSide>('player-one');
  const [error, setError] = useState<string | null>(null);

  const playerCard = playerDeck[0] ?? null;
  const cpuCard = cpuDeck[0] ?? null;
  const activeCard = currentTurn === 'player-one' ? playerCard : cpuCard;
  const round = history.length + 1;

  const winner = useMemo(() => {
    if (gameMode === 'local-pvp') return localWinnerLabel(playerDeck.length, cpuDeck.length);
    if (playerDeck.length === 0 && cpuDeck.length === 0) return 'Empate';
    if (playerDeck.length === 0 && cpuDeck.length > 0) return 'CPU';
    if (cpuDeck.length === 0 && playerDeck.length > 0) return 'Você';
    return null;
  }, [cpuDeck.length, gameMode, playerDeck.length]);

  async function startGame({
    candidates = [],
    cards,
    playerCards,
    cpuCards,
    difficulty: nextDifficulty,
    gameMode: nextGameMode = 'cpu',
    loadDetail
  }: StartGameParams) {
    setStatus('loading');
    setError(null);
    setHistory([]);
    setDisputePile([]);
    setRoundResult(null);
    setSelectedAttribute(null);
    setDifficulty(nextDifficulty);
    setGameMode(nextGameMode);
    setCurrentTurn('player-one');

    try {
      if (playerCards?.length && cpuCards?.length) {
        setPlayerDeck(playerCards);
        setCpuDeck(cpuCards);
        setStatus('ready');
        return;
      }

      const readyCards = cards ?? [];
      const pool = readyCards.length > 0 ? [] : buildDeckPool(candidates, 40);

      if (readyCards.length < 4 && pool.length < 4) {
        throw new Error('Poucos Pokémon disponíveis para montar uma partida.');
      }

      const localCards = await Promise.all(
        pool.map(async (pokemon) => createTrunfoCard(pokemon, await loadDetail(pokemon)))
      );
      const { playerDeck: nextPlayerDeck, cpuDeck: nextCpuDeck } = splitDeck(
        readyCards.length > 0 ? readyCards : localCards
      );

      setPlayerDeck(nextPlayerDeck);
      setCpuDeck(nextCpuDeck);
      setStatus('ready');
    } catch (exception) {
      setStatus('setup');
      setError(exception instanceof Error ? exception.message : 'Não foi possível iniciar a partida.');
    }
  }

  function playRound(attribute: TrunfoAttributeKey) {
    if (!playerCard || !cpuCard || status !== 'ready') return;

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
    if (!playerCard || !cpuCard || !roundResult) return;

    const nextState = applyRoundResult(roundResult.result, playerDeck, cpuDeck, disputePile);

    setPlayerDeck(nextState.playerOneDeck);
    setCpuDeck(nextState.playerTwoDeck);
    setDisputePile(nextState.disputePile);

    setCurrentTurn(gameMode === 'local-pvp' ? nextTurnForResult(roundResult.result, currentTurn) : 'player-one');
    setSelectedAttribute(null);
    setRoundResult(null);

    const nextPlayerCount = nextState.playerOneDeck.length;
    const nextCpuCount = nextState.playerTwoDeck.length;
    setStatus(nextPlayerCount === 0 || nextCpuCount === 0 ? 'finished' : 'ready');
  }

  function cpuSuggestion() {
    return gameMode === 'cpu' && cpuCard ? chooseCpuAttribute(cpuCard, difficulty) : null;
  }

  function resetGame() {
    setStatus('setup');
    setPlayerDeck([]);
    setCpuDeck([]);
    setDisputePile([]);
    setHistory([]);
    setSelectedAttribute(null);
    setRoundResult(null);
    setCurrentTurn('player-one');
    setError(null);
  }

  return {
    status,
    playerDeck,
    cpuDeck,
    disputePile,
    playerCard,
    cpuCard,
    activeCard,
    history,
    selectedAttribute,
    roundResult,
    round,
    winner,
    error,
    gameMode,
    currentTurn,
    startGame,
    playRound,
    nextRound,
    resetGame,
    cpuSuggestion
  };
}

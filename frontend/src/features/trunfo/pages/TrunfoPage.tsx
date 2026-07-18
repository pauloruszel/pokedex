import { useMemo, useState } from 'react';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import type { PokemonDetail, PokemonSummary } from '../../pokemon/types/pokemon';
import { BattlePanel } from '../components/BattlePanel';
import { DeckDraft } from '../components/DeckDraft';
import { GameSetup } from '../components/GameSetup';
import { RoundHistory } from '../components/RoundHistory';
import { trunfoApi } from '../api/trunfoApi';
import { createTrunfoCardFromApi } from '../api/trunfoMapper';
import { useTrunfoGame } from '../hooks/useTrunfoGame';
import type { TrunfoCardModel } from '../types/trunfoCard';
import type { PlayerSide, TrunfoSetup } from '../types/trunfoGame';
import { buildBalancedCpuDeck, buildDeckPool, TRUNFO_DECK_SIZE } from '../utils/trunfoDeck';
import { createTrunfoCard } from '../utils/trunfoCardFactory';

const DRAFT_PAGE_SIZE = 60;

type Props = {
  favorites: PokemonSummary[];
  types: string[];
  loadDetail: (pokemon: PokemonSummary) => Promise<PokemonDetail>;
};

export function TrunfoPage({ favorites, types, loadDetail }: Props) {
  const messages = useMessages();
  const [setup, setSetup] = useState<TrunfoSetup>({
    mode: 'all',
    deckSelection: 'auto',
    gameMode: 'cpu',
    type: types[0] ?? 'normal',
    difficulty: 'balanced'
  });
  const [setupError, setSetupError] = useState<string | null>(null);
  const [isPreparing, setIsPreparing] = useState(false);
  const [draftCards, setDraftCards] = useState<TrunfoCardModel[]>([]);
  const [draftOffset, setDraftOffset] = useState(0);
  const [hasMoreDraftCards, setHasMoreDraftCards] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [draftPlayer, setDraftPlayer] = useState<PlayerSide>('player-one');
  const [playerOneCards, setPlayerOneCards] = useState<TrunfoCardModel[]>([]);
  const game = useTrunfoGame();

  const candidates = useMemo(() => setup.mode === 'favorites' ? favorites : [], [favorites, setup.mode]);

  async function startGame() {
    if (setup.deckSelection === 'manual') {
      await prepareDraft();
      return;
    }

    setSetupError(null);
    setIsPreparing(true);

    try {
      const cards = setup.mode === 'favorites'
        ? undefined
        : (await trunfoApi.cards(40, setup.difficulty, setup.mode === 'type' ? setup.type : undefined)).map(createTrunfoCardFromApi);
      await game.startGame({
        candidates,
        cards,
        difficulty: setup.difficulty,
        gameMode: setup.gameMode,
        loadDetail
      });
    } catch {
      setSetupError(messages.trunfo.setupError);
    } finally {
      setIsPreparing(false);
    }
  }

  async function prepareDraft() {
    setSetupError(null);
    setIsPreparing(true);
    setDraftPlayer('player-one');
    setPlayerOneCards([]);

    try {
      const cards = setup.mode === 'favorites'
        ? await Promise.all(buildDeckPool(candidates, Math.max(candidates.length, 40)).map(async (pokemon) => createTrunfoCard(pokemon, await loadDetail(pokemon))))
        : await loadDraftCards(0);

      if (cards.length < 8) throw new Error(messages.trunfo.setupError);

      setDraftCards(cards);
      setDraftOffset(cards.length);
      setHasMoreDraftCards(setup.mode !== 'favorites' && cards.length === DRAFT_PAGE_SIZE);
      setSelectedIds(new Set());
    } catch {
      setSetupError(messages.trunfo.setupError);
    } finally {
      setIsPreparing(false);
    }
  }

  async function loadDraftCards(offset: number) {
    return (await trunfoApi.cards(DRAFT_PAGE_SIZE, setup.difficulty, setup.mode === 'type' ? setup.type : undefined, offset)).map(createTrunfoCardFromApi);
  }

  async function loadMoreDraftCards() {
    setSetupError(null);
    setIsPreparing(true);
    try {
      const nextCards = await loadDraftCards(draftOffset);
      setDraftCards((current) => mergeCards(current, nextCards));
      setDraftOffset((current) => current + DRAFT_PAGE_SIZE);
      setHasMoreDraftCards(nextCards.length === DRAFT_PAGE_SIZE);
    } catch {
      setSetupError(messages.trunfo.setupError);
    } finally {
      setIsPreparing(false);
    }
  }

  function toggleDraftCard(card: TrunfoCardModel) {
    setSelectedIds((current) => {
      const next = new Set(current);
      if (next.has(card.id)) next.delete(card.id);
      else if (next.size < TRUNFO_DECK_SIZE) next.add(card.id);
      return next;
    });
  }

  async function startSelectedGame() {
    const selectedCards = draftCards.filter((card) => selectedIds.has(card.id));
    if (selectedCards.length < 4) {
      setSetupError(messages.trunfo.draftError);
      return;
    }

    if (setup.gameMode === 'local-pvp' && draftPlayer === 'player-one') {
      setPlayerOneCards(selectedCards);
      setSelectedIds(new Set());
      setDraftPlayer('player-two');
      setSetupError(null);
      return;
    }

    const opponentCards = setup.gameMode === 'local-pvp'
      ? selectedCards
      : buildBalancedCpuDeck(draftCards, selectedCards);
    const firstPlayerCards = setup.gameMode === 'local-pvp' ? playerOneCards : selectedCards;

    const invalidDecks = setup.gameMode === 'local-pvp'
      ? opponentCards.length !== firstPlayerCards.length
      : opponentCards.length < firstPlayerCards.length;
    if (invalidDecks) {
      setSetupError(setup.gameMode === 'local-pvp' ? 'Os dois jogadores precisam escolher a mesma quantidade de cartas.' : messages.trunfo.draftError);
      return;
    }

    await game.startGame({
      playerCards: firstPlayerCards,
      cpuCards: opponentCards,
      difficulty: setup.difficulty,
      gameMode: setup.gameMode,
      loadDetail
    });
  }

  function backToSetup() {
    setDraftCards([]);
    setDraftOffset(0);
    setHasMoreDraftCards(false);
    setSelectedIds(new Set());
    setDraftPlayer('player-one');
    setPlayerOneCards([]);
    setSetupError(null);
  }

  return (
    <section className="trunfo-page">
      {game.status === 'setup' || game.status === 'loading' ? (
        draftCards.length > 0 ? (
          <>
            {setup.gameMode === 'local-pvp' && (
              <div className="trunfo-dispute-banner">
                {draftPlayer === 'player-one' ? 'Jogador 1: escolha seu baralho.' : `Jogador 2: escolha ${playerOneCards.length} cartas sem olhar as escolhas do Jogador 1.`}
              </div>
            )}
            <DeckDraft
              cards={draftCards}
              selectedIds={selectedIds}
              deckSize={setup.gameMode === 'local-pvp' && draftPlayer === 'player-two' ? playerOneCards.length : TRUNFO_DECK_SIZE}
              isLoading={game.status === 'loading' || isPreparing}
              canLoadMore={setup.mode !== 'favorites' && hasMoreDraftCards}
              error={setupError}
              onToggle={toggleDraftCard}
              onConfirm={startSelectedGame}
              onBack={backToSetup}
              onLoadMore={loadMoreDraftCards}
            />
          </>
        ) : (
          <GameSetup
            setup={setup}
            types={types}
            favoritesCount={favorites.length}
            isLoading={game.status === 'loading' || isPreparing}
            error={setupError ?? (game.error ? messages.trunfo.setupError : null)}
            onChange={setSetup}
            onStart={startGame}
          />
        )
      ) : (
        <div className="trunfo-game-layout">
          <BattlePanel
            status={game.status}
            round={game.round}
            playerDeckCount={game.playerDeck.length}
            cpuDeckCount={game.cpuDeck.length}
            disputePileCount={game.disputePile.length}
            playerCard={game.playerCard}
            cpuCard={game.cpuCard}
            selectedAttribute={game.selectedAttribute}
            roundResult={game.roundResult}
            winner={game.winner}
            cpuSuggestion={game.cpuSuggestion()}
            gameMode={game.gameMode}
            currentTurn={game.currentTurn}
            onPlay={game.playRound}
            onNext={game.nextRound}
            onReset={() => {
              game.resetGame();
              backToSetup();
            }}
          />
          <RoundHistory history={game.history} gameMode={game.gameMode} />
        </div>
      )}
    </section>
  );
}

function mergeCards(current: TrunfoCardModel[], next: TrunfoCardModel[]) {
  const ids = new Set(current.map((card) => card.id));
  return [...current, ...next.filter((card) => !ids.has(card.id))];
}

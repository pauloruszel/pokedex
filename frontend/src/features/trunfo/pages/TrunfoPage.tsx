import { useEffect, useMemo, useState } from 'react';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import { formatPokemonName } from '../../../shared/utils/format';
import type { PokemonDetail, PokemonSummary } from '../../pokemon/types/pokemon';
import { BattlePanel } from '../components/BattlePanel';
import { DeckDraft } from '../components/DeckDraft';
import { GameSetup } from '../components/GameSetup';
import { RoundHistory } from '../components/RoundHistory';
import { TrunfoCard } from '../components/TrunfoCard';
import { trunfoApi } from '../api/trunfoApi';
import type { TrunfoRoomDto, TrunfoRoomRoundDto } from '../api/trunfoDto';
import { createTrunfoCardFromApi } from '../api/trunfoMapper';
import { useTrunfoGame } from '../hooks/useTrunfoGame';
import type { TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfoCard';
import type { PlayerSide, TrunfoSetup } from '../types/trunfoGame';
import { ATTRIBUTE_OPTIONS, formatAttributeValue, getAttributeShortLabel } from '../utils/trunfoAttributes';
import { buildBalancedCpuDeck, buildDeckPool, TRUNFO_DECK_SIZE } from '../utils/trunfoDeck';
import { createTrunfoCard } from '../utils/trunfoCardFactory';

const DRAFT_PAGE_SIZE = 60;
const ONLINE_SESSION_KEY = 'trunfo-online-room';

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
  const [onlineName, setOnlineName] = useState('Jogador');
  const [onlineCode, setOnlineCode] = useState('');
  const [onlineDeckSize, setOnlineDeckSize] = useState(8);
  const [onlineRoom, setOnlineRoom] = useState<TrunfoRoomDto | null>(null);
  const [onlineDraftCards, setOnlineDraftCards] = useState<TrunfoCardModel[]>([]);
  const [onlineSelectedIds, setOnlineSelectedIds] = useState<Set<number>>(new Set());
  const [onlineError, setOnlineError] = useState<string | null>(null);
  const [onlineLoading, setOnlineLoading] = useState(false);
  const [dismissedOnlineRound, setDismissedOnlineRound] = useState<number | null>(null);
  const game = useTrunfoGame();

  const candidates = useMemo(() => setup.mode === 'favorites' ? favorites : [], [favorites, setup.mode]);

  useEffect(() => {
    const saved = localStorage.getItem(ONLINE_SESSION_KEY);
    if (!saved) return;

    try {
      const session = JSON.parse(saved) as { code: string; playerToken: string };
      trunfoApi.room(session.code, session.playerToken).then(setOnlineRoom).catch(() => localStorage.removeItem(ONLINE_SESSION_KEY));
    } catch {
      localStorage.removeItem(ONLINE_SESSION_KEY);
    }
  }, []);

  useEffect(() => {
    if (!onlineRoom || onlineRoom.state === 'FINISHED') return;

    const id = window.setInterval(() => {
      trunfoApi.room(onlineRoom.code, onlineRoom.playerToken).then(setOnlineRoom).catch(() => undefined);
    }, 3000);
    return () => window.clearInterval(id);
  }, [onlineRoom]);

  useEffect(() => {
    if (onlineRoom?.state !== 'DECK_SELECTION' || onlineDraftCards.length > 0) return;

    setOnlineLoading(true);
    trunfoApi.cards(80, onlineRoom.difficulty, onlineRoom.type ?? undefined)
      .then((cards) => setOnlineDraftCards(cards.map(createTrunfoCardFromApi)))
      .catch(() => setOnlineError(messages.trunfo.setupError))
      .finally(() => setOnlineLoading(false));
  }, [messages.trunfo.setupError, onlineDraftCards.length, onlineRoom]);

  async function startGame() {
    if (setup.gameMode === 'online-pvp') {
      await createOnlineRoom();
      return;
    }

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
      setSetupError(setup.gameMode === 'local-pvp' ? 'Cada jogador precisa escolher pelo menos 4 cartas e a mesma quantidade.' : messages.trunfo.draftError);
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

  async function createOnlineRoom() {
    setOnlineLoading(true);
    setOnlineError(null);
    try {
      saveOnlineRoom(await trunfoApi.createRoom({
        nickname: onlineName,
        mode: setup.mode,
        difficulty: setup.difficulty,
        deckSelection: setup.deckSelection,
        deckSize: onlineDeckSize,
        type: setup.mode === 'type' ? setup.type : undefined
      }));
    } catch {
      setOnlineError(messages.trunfo.setupError);
    } finally {
      setOnlineLoading(false);
    }
  }

  async function joinOnlineRoom() {
    if (!onlineCode.trim()) return;

    setOnlineLoading(true);
    setOnlineError(null);
    try {
      saveOnlineRoom(await trunfoApi.joinRoom(onlineCode.trim(), onlineName));
    } catch {
      setOnlineError('Nao foi possivel entrar nessa sala.');
    } finally {
      setOnlineLoading(false);
    }
  }

  async function playOnlineRound(attribute: TrunfoAttributeKey) {
    if (!onlineRoom) return;

    setOnlineLoading(true);
    setOnlineError(null);
    try {
      setDismissedOnlineRound(null);
      setOnlineRoom(await trunfoApi.playRoomRound(onlineRoom.code, onlineRoom.playerToken, attribute));
    } catch {
      setOnlineError('Jogada recusada pelo servidor.');
    } finally {
      setOnlineLoading(false);
    }
  }

  async function confirmOnlineDeck() {
    if (!onlineRoom) return;
    if (onlineSelectedIds.size !== onlineRoom.deckSize) {
      setOnlineError(`Escolha exatamente ${onlineRoom.deckSize} cartas.`);
      return;
    }

    setOnlineLoading(true);
    setOnlineError(null);
    try {
      setOnlineRoom(await trunfoApi.confirmDeck(onlineRoom.code, onlineRoom.playerToken, Array.from(onlineSelectedIds)));
      setOnlineSelectedIds(new Set());
    } catch (error) {
      setOnlineError(error instanceof Error ? error.message : 'Nao foi possivel confirmar o baralho.');
    } finally {
      setOnlineLoading(false);
    }
  }

  function saveOnlineRoom(room: TrunfoRoomDto) {
    setOnlineRoom(room);
    setOnlineDraftCards([]);
    setOnlineSelectedIds(new Set());
    localStorage.setItem(ONLINE_SESSION_KEY, JSON.stringify({ code: room.code, playerToken: room.playerToken }));
  }

  async function leaveOnlineRoom() {
    if (onlineRoom) {
      await trunfoApi.leaveRoom(onlineRoom.code, onlineRoom.playerToken).catch(() => undefined);
    }
    setOnlineRoom(null);
    setOnlineError(null);
    localStorage.removeItem(ONLINE_SESSION_KEY);
  }

  if (onlineRoom) {
    if (onlineRoom.state === 'DECK_SELECTION') {
      return (
        <section className="trunfo-page">
          <div className="trunfo-dispute-banner">
            {onlineRoom.playerDeckCount > 0 ? 'Baralho confirmado. Aguardando o adversario.' : `Escolha ${onlineRoom.deckSize} cartas para a sala ${onlineRoom.code}.`}
          </div>
          {onlineRoom.playerDeckCount > 0 ? (
            <OnlineRoom
              room={onlineRoom}
              isLoading={onlineLoading}
              error={onlineError}
              dismissedRound={dismissedOnlineRound}
              onPlay={playOnlineRound}
              onNextRound={(round) => setDismissedOnlineRound(round)}
              onRefresh={() => trunfoApi.room(onlineRoom.code, onlineRoom.playerToken).then(setOnlineRoom).catch(() => setOnlineError('Nao foi possivel atualizar a sala.'))}
              onLeave={leaveOnlineRoom}
            />
          ) : (
            <DeckDraft
              cards={onlineDraftCards}
              selectedIds={onlineSelectedIds}
              deckSize={onlineRoom.deckSize}
              isLoading={onlineLoading}
              canLoadMore={false}
              error={onlineError}
              description="Seu adversario nao ve as cartas que voce escolheu."
              onToggle={(card) => setOnlineSelectedIds((current) => toggleCardId(current, card.id, onlineRoom.deckSize))}
              onConfirm={confirmOnlineDeck}
              onBack={leaveOnlineRoom}
              onLoadMore={() => undefined}
            />
          )}
        </section>
      );
    }

    return (
      <section className="trunfo-page">
        <OnlineRoom
          room={onlineRoom}
          isLoading={onlineLoading}
          error={onlineError}
          dismissedRound={dismissedOnlineRound}
          onPlay={playOnlineRound}
          onNextRound={(round) => setDismissedOnlineRound(round)}
          onRefresh={() => trunfoApi.room(onlineRoom.code, onlineRoom.playerToken).then(setOnlineRoom).catch(() => setOnlineError('Nao foi possivel atualizar a sala.'))}
          onLeave={leaveOnlineRoom}
        />
      </section>
    );
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
              description={setup.gameMode === 'local-pvp' ? 'Cada jogador escolhe seu baralho em uma etapa separada.' : undefined}
              onToggle={toggleDraftCard}
              onConfirm={startSelectedGame}
              onBack={backToSetup}
              onLoadMore={loadMoreDraftCards}
            />
          </>
        ) : (
          <>
            <GameSetup
              setup={setup}
              types={types}
              favoritesCount={favorites.length}
              isLoading={game.status === 'loading' || isPreparing || onlineLoading}
              error={setupError ?? onlineError ?? (game.error ? messages.trunfo.setupError : null)}
              onChange={setSetup}
              onStart={startGame}
            />
            {setup.gameMode === 'online-pvp' && (
              <div className="trunfo-online-panel">
                <label>
                  Apelido
                  <input value={onlineName} onChange={(event) => setOnlineName(event.target.value)} />
                </label>
                <label>
                  Codigo da sala
                  <input value={onlineCode} onChange={(event) => setOnlineCode(event.target.value.toUpperCase())} placeholder="PKM-4821" />
                </label>
                <div className="trunfo-online-size">
                  <span>Tamanho</span>
                  <div className="trunfo-segmented">
                    <button className={onlineDeckSize === 8 ? 'trunfo-segment trunfo-segment--active' : 'trunfo-segment'} onClick={() => setOnlineDeckSize(8)}>Rápida · 8</button>
                    <button className={onlineDeckSize === 20 ? 'trunfo-segment trunfo-segment--active' : 'trunfo-segment'} onClick={() => setOnlineDeckSize(20)}>Normal · 20</button>
                  </div>
                </div>
                <button className="secondary-control" disabled={onlineLoading || !onlineCode.trim()} onClick={joinOnlineRoom}>Entrar na sala</button>
              </div>
            )}
          </>
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

function toggleCardId(current: Set<number>, id: number, limit: number) {
  const next = new Set(current);
  if (next.has(id)) next.delete(id);
  else if (next.size < limit) next.add(id);
  return next;
}

function OnlineRoom({ room, isLoading, error, dismissedRound, onPlay, onNextRound, onRefresh, onLeave }: {
  room: TrunfoRoomDto;
  isLoading: boolean;
  error: string | null;
  dismissedRound: number | null;
  onPlay: (attribute: TrunfoAttributeKey) => void;
  onNextRound: (round: number) => void;
  onRefresh: () => void;
  onLeave: () => void;
}) {
  const messages = useMessages();
  const isTurn = room.state === 'IN_PROGRESS' && room.currentTurn === room.playerSide;
  const lastRound = room.lastRound?.round === dismissedRound ? null : room.lastRound;
  const ownLastCard = lastRound ? cardForSide(lastRound, room.playerSide) : null;
  const opponentLastCard = lastRound ? cardForSide(lastRound, room.playerSide === 'player-one' ? 'player-two' : 'player-one') : null;
  const turnLabel = isTurn ? 'Sua vez' : room.state === 'WAITING_FOR_PLAYER' ? 'Aguardando jogador' : 'Vez do adversario';
  const hasActionPanel = Boolean(room.winner || lastRound);

  return (
    <div className="trunfo-game-layout">
      <section className={hasActionPanel ? 'trunfo-battle trunfo-battle--action' : 'trunfo-battle'}>
        <header className="trunfo-scoreboard">
          <div>
            <span>{room.playerSide === 'player-one' ? room.playerOneName : room.playerTwoName}</span>
            <strong>{room.playerDeckCount}</strong>
          </div>
          <div className="trunfo-round-pill">Sala {room.code} · rodada {room.round}<small> · {turnLabel}</small></div>
          <div>
            <span>{room.playerSide === 'player-one' ? room.playerTwoName ?? 'Aguardando' : room.playerOneName}</span>
            <strong>{room.opponentDeckCount}</strong>
          </div>
        </header>

        {room.state === 'WAITING_FOR_PLAYER' && <div className="trunfo-dispute-banner">Compartilhe o codigo {room.code} e aguarde o outro jogador.</div>}
        {error && <div className="trunfo-dispute-banner">{error}</div>}

        <div className="trunfo-table">
          <TrunfoCard card={lastRound ? ownLastCard : room.playerCard ? createTrunfoCardFromApi(room.playerCard) : null} side="player" selectedAttribute={lastRound?.attribute} />
          <div className={hasActionPanel ? 'trunfo-vs-panel trunfo-vs-panel--action' : 'trunfo-vs-panel'}>
            {room.winner ? (
              <>
                <span className="trunfo-result">Fim</span>
                <strong>{room.winner} venceu</strong>
              </>
            ) : lastRound ? (
              <>
                <OnlineRoundResult round={lastRound} side={room.playerSide} />
                <button className="primary-control" onClick={() => onNextRound(lastRound.round)}>Continuar</button>
              </>
            ) : (
              <>
                <span className="trunfo-result">{isTurn ? 'Sua vez' : 'Aguardando'}</span>
                <p>{isTurn ? 'Escolha um atributo. O servidor processa a rodada.' : 'Aguarde a jogada do adversario.'}</p>
              </>
            )}
            <button className="secondary-control" onClick={onRefresh} disabled={isLoading}>Atualizar</button>
            <button className="secondary-control" onClick={onLeave}>Sair</button>
          </div>
          <TrunfoCard card={lastRound ? opponentLastCard : null} side="cpu" isHidden={!lastRound} hiddenLabel="Carta do adversario" selectedAttribute={lastRound?.attribute} />
        </div>

        {!hasActionPanel && (
          <div className="trunfo-attribute-picker">
            {ATTRIBUTE_OPTIONS.map((option) => (
              <button
                className="trunfo-attribute-button"
                disabled={!isTurn || isLoading || !room.playerCard}
                key={option.key}
                onClick={() => onPlay(option.key)}
              >
                <span>{getAttributeShortLabel(option.key, messages)}</span>
                <strong>{room.playerCard ? formatAttributeValue(option.key, room.playerCard.attributes[option.key]) : '-'}</strong>
              </button>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function OnlineRoundResult({ round, side }: { round: TrunfoRoomRoundDto; side: PlayerSide }) {
  const ownWon = (side === 'player-one' && round.result === 'player') || (side === 'player-two' && round.result === 'cpu');

  return (
    <>
      <span className={`trunfo-result trunfo-result--${round.result}`}>{round.result === 'draw' ? 'Empate' : ownWon ? 'Voce venceu a rodada' : 'Adversario venceu a rodada'}</span>
      <strong>{round.attribute}</strong>
      <p>{formatPokemonName(round.playerOneCard.name)} {round.playerOneValue} x {round.playerTwoValue} {formatPokemonName(round.playerTwoCard.name)}</p>
      {round.result === 'draw' && <small>Monte acumulado: {round.potSize}</small>}
    </>
  );
}

function cardForSide(round: TrunfoRoomRoundDto, side: PlayerSide) {
  return createTrunfoCardFromApi(side === 'player-one' ? round.playerOneCard : round.playerTwoCard);
}

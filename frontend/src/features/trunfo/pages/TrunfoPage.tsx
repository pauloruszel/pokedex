import { useMemo, useState } from 'react';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import type { PokemonDetail, PokemonSummary } from '../../pokemon/types/pokemon';
import { BattlePanel } from '../components/BattlePanel';
import { GameSetup } from '../components/GameSetup';
import { RoundHistory } from '../components/RoundHistory';
import { trunfoApi } from '../api/trunfoApi';
import { createTrunfoCardFromApi } from '../api/trunfoMapper';
import { useTrunfoGame } from '../hooks/useTrunfoGame';
import type { TrunfoSetup } from '../types/trunfoGame';

type Props = {
  favorites: PokemonSummary[];
  types: string[];
  loadDetail: (pokemon: PokemonSummary) => Promise<PokemonDetail>;
};

export function TrunfoPage({ favorites, types, loadDetail }: Props) {
  const messages = useMessages();
  const [setup, setSetup] = useState<TrunfoSetup>({
    mode: 'all',
    type: types[0] ?? 'normal',
    difficulty: 'balanced'
  });
  const [setupError, setSetupError] = useState<string | null>(null);
  const [isPreparing, setIsPreparing] = useState(false);
  const game = useTrunfoGame();

  const candidates = useMemo(() => {
    if (setup.mode === 'favorites') return favorites;
    return [];
  }, [favorites, setup.mode]);

  async function startGame() {
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
        loadDetail
      });
    } catch {
      setSetupError(messages.trunfo.setupError);
    } finally {
      setIsPreparing(false);
    }
  }

  return (
    <section className="trunfo-page">
      {game.status === 'setup' || game.status === 'loading' ? (
        <GameSetup
          setup={setup}
          types={types}
          favoritesCount={favorites.length}
          isLoading={game.status === 'loading' || isPreparing}
          error={setupError ?? (game.error ? messages.trunfo.setupError : null)}
          onChange={setSetup}
          onStart={startGame}
        />
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
            onPlay={game.playRound}
            onNext={game.nextRound}
            onReset={game.resetGame}
          />
          <RoundHistory history={game.history} />
        </div>
      )}
    </section>
  );
}

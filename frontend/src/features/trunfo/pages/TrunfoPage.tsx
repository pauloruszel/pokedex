import { useMemo, useState } from 'react';
import type { PokemonDetail, PokemonSummary } from '../../pokemon/types/pokemon';
import { BattlePanel } from '../components/BattlePanel';
import { GameSetup } from '../components/GameSetup';
import { RoundHistory } from '../components/RoundHistory';
import { useTrunfoGame } from '../hooks/useTrunfoGame';
import type { TrunfoSetup } from '../types/trunfo';

type Props = {
  favorites: PokemonSummary[];
  types: string[];
  getCandidates: (setup: TrunfoSetup) => Promise<PokemonSummary[]>;
  loadDetail: (pokemon: PokemonSummary) => Promise<PokemonDetail>;
};

export function TrunfoPage({ favorites, types, getCandidates, loadDetail }: Props) {
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
      const nextCandidates = setup.mode === 'favorites' ? candidates : await getCandidates(setup);
      await game.startGame({
        candidates: nextCandidates,
        difficulty: setup.difficulty,
        loadDetail
      });
    } catch {
      setSetupError('Não foi possível montar o baralho agora.');
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
          error={setupError ?? game.error}
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

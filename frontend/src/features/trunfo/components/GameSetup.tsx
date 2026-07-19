import type { Dispatch, SetStateAction } from 'react';
import { Bot, Dices, Hand, Layers3, Link2, Play, Shuffle, SlidersHorizontal, Users } from 'lucide-react';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import type {
  TrunfoDeckSelection,
  TrunfoDifficulty,
  TrunfoGameMode,
  TrunfoMode,
  TrunfoSetup
} from '../types/trunfoGame';

type Props = {
  setup: TrunfoSetup;
  types: string[];
  favoritesCount: number;
  isLoading: boolean;
  error: string | null;
  onChange: Dispatch<SetStateAction<TrunfoSetup>>;
  onStart: () => void;
};

export function GameSetup({ setup, types, favoritesCount, isLoading, error, onChange, onStart }: Props) {
  const messages = useMessages();
  const isLocalPvp = setup.gameMode === 'local-pvp';
  const isOnlinePvp = setup.gameMode === 'online-pvp';
  const difficulties: Array<{ value: TrunfoDifficulty; label: string; description: string }> = [
    { value: 'balanced', label: messages.trunfo.balanced, description: messages.trunfo.balancedDescription },
    { value: 'casual', label: messages.trunfo.casual, description: messages.trunfo.casualDescription },
    { value: 'expert', label: messages.trunfo.expert, description: messages.trunfo.expertDescription }
  ];
  const modes: Array<{ value: TrunfoMode; label: string }> = [
    { value: 'all', label: messages.trunfo.modeAll },
    { value: 'favorites', label: messages.trunfo.modeFavorites },
    { value: 'type', label: messages.trunfo.modeType }
  ];
  const gameModes: Array<{ value: TrunfoGameMode; label: string; description: string; icon: typeof Bot }> = [
    { value: 'cpu', label: 'Contra a CPU', description: 'Modo clássico contra o computador.', icon: Bot },
    { value: 'local-pvp', label: 'Dois jogadores', description: 'Joguem no mesmo aparelho, alternando a vez.', icon: Users },
    { value: 'online-pvp', label: 'Sala privada', description: 'Crie ou entre por código em outro celular.', icon: Link2 }
  ];
  const deckSelections: Array<{ value: TrunfoDeckSelection; label: string; description: string; icon: typeof Shuffle }> = [
    {
      value: 'auto',
      label: messages.trunfo.autoDeck,
      description: isLocalPvp || isOnlinePvp
        ? 'O sistema monta e embaralha os dois baralhos automaticamente.'
        : messages.trunfo.autoDeckDescription,
      icon: Shuffle
    },
    {
      value: 'manual',
      label: messages.trunfo.manualDeck,
      description: isLocalPvp || isOnlinePvp
        ? 'Cada jogador escolhe seu próprio baralho antes da partida.'
        : messages.trunfo.manualDeckDescription,
      icon: Hand
    }
  ];
  const visibleError = isOnlinePvp && error === messages.trunfo.setupError
    ? 'Não foi possível criar a sala agora. Tente novamente em instantes.'
    : error;
  const startLabel = isOnlinePvp
    ? (isLoading ? 'Criando sala...' : 'Criar sala')
    : (isLoading ? messages.trunfo.loadingDecks : messages.trunfo.start);

  return (
    <section className="trunfo-setup">
      <div className="trunfo-setup-copy">
        <span className="eyebrow-line"><Dices size={16} /> {messages.trunfo.setupEyebrow}</span>
        <h2>{isLocalPvp ? 'Monte dois baralhos e dispute jogador contra jogador.' : isOnlinePvp ? 'Crie uma sala privada e jogue em dois celulares.' : messages.trunfo.setupTitle}</h2>
        <p>{isLocalPvp ? 'Dois jogadores no mesmo aparelho, com cartas ocultas e turno alternado pelo vencedor da rodada.' : isOnlinePvp ? 'O servidor guarda a sala e valida cada rodada pelo código.' : messages.trunfo.setupDescription}</p>
      </div>

      <div className="trunfo-setup-panel">
        <div className="trunfo-control-group">
          <span><Users size={16} /> Adversário</span>
          <div className="trunfo-difficulty-grid">
            {gameModes.map((mode) => {
              const Icon = mode.icon;
              return (
                <button
                  className={setup.gameMode === mode.value ? 'trunfo-difficulty trunfo-difficulty--active' : 'trunfo-difficulty'}
                  key={mode.value}
                  onClick={() => onChange((current) => ({ ...current, gameMode: mode.value, deckSelection: mode.value === 'online-pvp' ? 'auto' : current.deckSelection }))}
                >
                  <strong><Icon size={15} /> {mode.label}</strong>
                  <small>{mode.description}</small>
                </button>
              );
            })}
          </div>
        </div>

        <div className="trunfo-control-group">
          <span><Layers3 size={16} /> {messages.trunfo.deck}</span>
          <div className="trunfo-segmented">
            {modes.map((mode) => (
              <button
                className={setup.mode === mode.value ? 'trunfo-segment trunfo-segment--active' : 'trunfo-segment'}
                key={mode.value}
                onClick={() => onChange((current) => ({ ...current, mode: mode.value }))}
              >
                {mode.label}
              </button>
            ))}
          </div>
          {setup.mode === 'favorites' && favoritesCount < 40 && <small>{messages.trunfo.favoriteRequirement}</small>}
          {setup.mode === 'type' && (
            <select value={setup.type} onChange={(event) => onChange((current) => ({ ...current, type: event.target.value }))}>
              {types.map((type) => (
                <option key={type} value={type}>{messages.types[type as keyof typeof messages.types] ?? type}</option>
              ))}
            </select>
          )}
        </div>

        {setup.gameMode === 'cpu' && (
          <div className="trunfo-control-group">
            <span><SlidersHorizontal size={16} /> {messages.trunfo.difficulty}</span>
            <div className="trunfo-difficulty-grid">
              {difficulties.map((difficulty) => (
                <button
                  className={setup.difficulty === difficulty.value ? 'trunfo-difficulty trunfo-difficulty--active' : 'trunfo-difficulty'}
                  key={difficulty.value}
                  onClick={() => onChange((current) => ({ ...current, difficulty: difficulty.value }))}
                >
                  <strong>{difficulty.label}</strong>
                  <small>{difficulty.description}</small>
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="trunfo-control-group">
          <span><Hand size={16} /> {messages.trunfo.deckSelection}</span>
          <div className="trunfo-difficulty-grid">
            {deckSelections.map((selection) => {
              const Icon = selection.icon;
              return (
                <button
                  className={setup.deckSelection === selection.value ? 'trunfo-difficulty trunfo-difficulty--active' : 'trunfo-difficulty'}
                  key={selection.value}
                  onClick={() => onChange((current) => ({ ...current, deckSelection: selection.value }))}
                >
                  <strong><Icon size={15} /> {selection.label}</strong>
                  <small>{selection.description}</small>
                </button>
              );
            })}
          </div>
        </div>

        {visibleError && <div className="error-box">{visibleError}</div>}

        <button className="primary-control trunfo-start-button" disabled={isLoading} onClick={onStart}>
          {isLoading ? <span className="pokeball-loader pokeball-loader--small" /> : <Play size={18} />}
          {startLabel}
        </button>
      </div>
    </section>
  );
}

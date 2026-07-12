import { Dices, Layers3, Play, SlidersHorizontal } from 'lucide-react';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import type { TrunfoDifficulty, TrunfoMode, TrunfoSetup } from '../types/trunfoGame';

type Props = {
  setup: TrunfoSetup;
  types: string[];
  favoritesCount: number;
  isLoading: boolean;
  error: string | null;
  onChange: (setup: TrunfoSetup) => void;
  onStart: () => void;
};

export function GameSetup({ setup, types, favoritesCount, isLoading, error, onChange, onStart }: Props) {
  const messages = useMessages();
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

  return (
    <section className="trunfo-setup">
      <div className="trunfo-setup-copy">
        <span className="eyebrow-line"><Dices size={16} /> {messages.trunfo.setupEyebrow}</span>
        <h2>{messages.trunfo.setupTitle}</h2>
        <p>{messages.trunfo.setupDescription}</p>
      </div>

      <div className="trunfo-setup-panel">
        <div className="trunfo-control-group">
          <span><Layers3 size={16} /> {messages.trunfo.deck}</span>
          <div className="trunfo-segmented">
            {modes.map((mode) => (
              <button
                className={setup.mode === mode.value ? 'trunfo-segment trunfo-segment--active' : 'trunfo-segment'}
                key={mode.value}
                onClick={() => onChange({ ...setup, mode: mode.value })}
              >
                {mode.label}
              </button>
            ))}
          </div>
          {setup.mode === 'favorites' && favoritesCount < 40 && (
            <small>{messages.trunfo.favoriteRequirement}</small>
          )}
          {setup.mode === 'type' && (
            <select value={setup.type} onChange={(event) => onChange({ ...setup, type: event.target.value })}>
              {types.map((type) => (
                <option key={type} value={type}>
                  {messages.types[type as keyof typeof messages.types] ?? type}
                </option>
              ))}
            </select>
          )}
        </div>

        <div className="trunfo-control-group">
          <span><SlidersHorizontal size={16} /> {messages.trunfo.difficulty}</span>
          <div className="trunfo-difficulty-grid">
            {difficulties.map((difficulty) => (
              <button
                className={setup.difficulty === difficulty.value ? 'trunfo-difficulty trunfo-difficulty--active' : 'trunfo-difficulty'}
                key={difficulty.value}
                onClick={() => onChange({ ...setup, difficulty: difficulty.value })}
              >
                <strong>{difficulty.label}</strong>
                <small>{difficulty.description}</small>
              </button>
            ))}
          </div>
        </div>

        {error && <div className="error-box">{error}</div>}

        <button className="primary-control trunfo-start-button" disabled={isLoading} onClick={onStart}>
          {isLoading ? <span className="pokeball-loader pokeball-loader--small" /> : <Play size={18} />}
          {isLoading ? messages.trunfo.loadingDecks : messages.trunfo.start}
        </button>
      </div>
    </section>
  );
}

import { Dices, Layers3, Play, SlidersHorizontal } from 'lucide-react';
import type { TrunfoDifficulty, TrunfoMode, TrunfoSetup } from '../types/trunfo';

type Props = {
  setup: TrunfoSetup;
  types: string[];
  favoritesCount: number;
  isLoading: boolean;
  error: string | null;
  onChange: (setup: TrunfoSetup) => void;
  onStart: () => void;
};

const DIFFICULTIES: Array<{ value: TrunfoDifficulty; label: string; description: string }> = [
  { value: 'balanced', label: 'Equilibrado', description: 'CPU escolhe entre bons atributos.' },
  { value: 'casual', label: 'Casual', description: 'CPU joga de forma imprevisível.' },
  { value: 'expert', label: 'Expert', description: 'CPU sempre pressiona seu melhor status.' }
];

const MODES: Array<{ value: TrunfoMode; label: string }> = [
  { value: 'all', label: 'Todos' },
  { value: 'favorites', label: 'Favoritos' },
  { value: 'type', label: 'Por tipo' }
];

export function GameSetup({ setup, types, favoritesCount, isLoading, error, onChange, onStart }: Props) {
  return (
    <section className="trunfo-setup">
      <div className="trunfo-setup-copy">
        <span className="eyebrow-line"><Dices size={16} /> Super Trunfo Pokémon</span>
        <h2>Monte seu baralho e vença a CPU carta por carta.</h2>
        <p>Uma partida rápida com 20 cartas para cada lado, atributos reais da Pokédex e raridade calculada pelo total de status.</p>
      </div>

      <div className="trunfo-setup-panel">
        <div className="trunfo-control-group">
          <span><Layers3 size={16} /> Baralho</span>
          <div className="trunfo-segmented">
            {MODES.map((mode) => (
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
            <small>Use pelo menos 40 favoritos para uma partida completa.</small>
          )}
          {setup.mode === 'type' && (
            <select value={setup.type} onChange={(event) => onChange({ ...setup, type: event.target.value })}>
              {types.map((type) => <option key={type} value={type}>{type}</option>)}
            </select>
          )}
        </div>

        <div className="trunfo-control-group">
          <span><SlidersHorizontal size={16} /> Dificuldade</span>
          <div className="trunfo-difficulty-grid">
            {DIFFICULTIES.map((difficulty) => (
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
          {isLoading ? 'Montando baralhos...' : 'Iniciar partida'}
        </button>
      </div>
    </section>
  );
}

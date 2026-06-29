import { History } from 'lucide-react';
import { formatPokemonName } from '../../../shared/utils/format';
import type { RoundHistoryItem } from '../types/trunfo';
import { formatAttributeValue, getAttributeLabel } from '../utils/trunfoRules';

type Props = {
  history: RoundHistoryItem[];
};

export function RoundHistory({ history }: Props) {
  return (
    <aside className="trunfo-history">
      <header>
        <span><History size={16} /> Histórico</span>
        <strong>{history.length}</strong>
      </header>

      {history.length === 0 ? (
        <p>Nenhuma rodada disputada ainda.</p>
      ) : (
        <div className="trunfo-history-list">
          {history.map((item) => (
            <article className={`trunfo-history-item trunfo-history-item--${item.result}`} key={item.round}>
              <span>Rodada {item.round}</span>
              <strong>{item.result === 'player' ? 'Você' : item.result === 'cpu' ? 'CPU' : 'Empate'}</strong>
              <small>
                {getAttributeLabel(item.attribute)}: {formatPokemonName(item.playerName)} {formatAttributeValue(item.attribute, item.playerValue)}
                {' '}x {formatAttributeValue(item.attribute, item.cpuValue)} {formatPokemonName(item.cpuName)}
              </small>
            </article>
          ))}
        </div>
      )}
    </aside>
  );
}

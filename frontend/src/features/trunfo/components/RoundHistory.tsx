import { History } from 'lucide-react';
import { useI18nFormat, useMessages } from '../../../shared/i18n/I18nProvider';
import { formatPokemonName } from '../../../shared/utils/format';
import type { RoundHistoryItem } from '../types/trunfoGame';
import { formatAttributeValue, getAttributeLabel } from '../utils/trunfoAttributes';

type Props = {
  history: RoundHistoryItem[];
};

export function RoundHistory({ history }: Props) {
  const messages = useMessages();
  const format = useI18nFormat();

  return (
    <aside className="trunfo-history">
      <header>
        <span><History size={16} /> {messages.trunfo.history}</span>
        <strong>{history.length}</strong>
      </header>

      {history.length === 0 ? (
        <p>{messages.trunfo.historyEmpty}</p>
      ) : (
        <div className="trunfo-history-list">
          {history.map((item) => (
            <article className={`trunfo-history-item trunfo-history-item--${item.result}`} key={item.round}>
              <span>{messages.trunfo.round} {item.round}</span>
              <strong>{item.result === 'player' ? messages.common.you : item.result === 'cpu' ? messages.common.cpu : messages.common.draw}</strong>
              <small>
                {getAttributeLabel(item.attribute, messages)}: {formatPokemonName(item.playerName)} {formatAttributeValue(item.attribute, item.playerValue)}
                {' '}x {formatAttributeValue(item.attribute, item.cpuValue)} {formatPokemonName(item.cpuName)}
              </small>
              {item.potSize > 2 && <em>{format(messages.trunfo.potCards, { count: item.potSize })}</em>}
            </article>
          ))}
        </div>
      )}
    </aside>
  );
}

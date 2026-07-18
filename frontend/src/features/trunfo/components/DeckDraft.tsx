import { Check, Play, RotateCcw, Search } from 'lucide-react';
import { useMemo, useState } from 'react';
import { TypeBadge } from '../../../shared/components/TypeBadge';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import { assetUrl } from '../../../shared/utils/assets';
import { formatPokemonName } from '../../../shared/utils/format';
import type { TrunfoCardModel } from '../types/trunfoCard';
import { getRarityLabel } from '../utils/trunfoAttributes';

type Props = {
  cards: TrunfoCardModel[];
  selectedIds: Set<number>;
  deckSize: number;
  isLoading: boolean;
  canLoadMore: boolean;
  error: string | null;
  description?: string;
  onToggle: (card: TrunfoCardModel) => void;
  onConfirm: () => void;
  onBack: () => void;
  onLoadMore: () => void;
};

export function DeckDraft({ cards, selectedIds, deckSize, isLoading, canLoadMore, error, description, onToggle, onConfirm, onBack, onLoadMore }: Props) {
  const messages = useMessages();
  const selectedCount = selectedIds.size;
  const canConfirm = selectedCount >= 4 && selectedCount <= deckSize && !isLoading;
  const [query, setQuery] = useState('');
  const visibleCards = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) return cards;
    return cards.filter((card) => card.summary.name.toLowerCase().includes(normalizedQuery) || String(card.id).includes(normalizedQuery));
  }, [cards, query]);

  return (
    <section className="trunfo-draft">
      <header className="trunfo-draft-header">
        <div>
          <span className="eyebrow-line">{messages.trunfo.draftEyebrow}</span>
          <h2>{messages.trunfo.draftTitle}</h2>
          <p>{description ?? messages.trunfo.draftDescription}</p>
        </div>
        <strong>{selectedCount}/{deckSize}</strong>
      </header>

      {error && <div className="error-box">{error}</div>}

      <label className="trunfo-draft-search">
        <Search size={17} />
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={messages.trunfo.draftSearch}
        />
      </label>

      <div className="trunfo-draft-grid">
        {visibleCards.map((card) => {
          const isSelected = selectedIds.has(card.id);
          const isDisabled = !isSelected && selectedCount >= deckSize;

          return (
            <button
              className={isSelected ? 'trunfo-draft-card trunfo-draft-card--selected' : 'trunfo-draft-card'}
              disabled={isDisabled}
              key={card.id}
              onClick={() => onToggle(card)}
            >
              <span className="trunfo-draft-check">{isSelected && <Check size={15} />}</span>
              <img src={assetUrl(card.summary.imageUrl)} alt={card.summary.name} loading="lazy" decoding="async" />
              <strong>{formatPokemonName(card.summary.name)}</strong>
              <small>{getRarityLabel(card.rarity, messages)} · {card.attributes.total}</small>
              <div className="type-list">
                {card.summary.types.map((type) => (
                  <TypeBadge key={type} type={type} label={messages.types[type as keyof typeof messages.types]} compact />
                ))}
              </div>
            </button>
          );
        })}
      </div>

      <footer className="trunfo-draft-actions">
        <button className="secondary-control" onClick={onBack}><RotateCcw size={17} /> {messages.trunfo.backToSetup}</button>
        {canLoadMore && (
          <button className="secondary-control" disabled={isLoading} onClick={onLoadMore}>
            {isLoading ? <span className="pokeball-loader pokeball-loader--small" /> : null}
            {messages.trunfo.loadMoreCards}
          </button>
        )}
        <button className="primary-control" disabled={!canConfirm} onClick={onConfirm}>
          {isLoading ? <span className="pokeball-loader pokeball-loader--small" /> : <Play size={18} />}
          {messages.trunfo.confirmDeck}
        </button>
      </footer>
    </section>
  );
}

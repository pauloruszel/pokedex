import { Crown, HelpCircle, ShieldQuestion, Sparkles } from 'lucide-react';
import type { CSSProperties } from 'react';
import { TypeBadge } from '../../../shared/components/TypeBadge';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import { assetUrl } from '../../../shared/utils/assets';
import { formatPokemonName, formatPokemonNumber } from '../../../shared/utils/format';
import { getTypeTheme } from '../../../shared/utils/typeTheme';
import type { TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfoCard';
import { ATTRIBUTE_OPTIONS, formatAttributeValue, getAttributeShortLabel, getRarityLabel } from '../utils/trunfoAttributes';

type Props = {
  card: TrunfoCardModel | null;
  side: 'player' | 'cpu';
  isHidden?: boolean;
  hiddenLabel?: string;
  selectedAttribute?: TrunfoAttributeKey | null;
  winningAttribute?: TrunfoAttributeKey | null;
};

export function TrunfoCard({ card, side, isHidden = false, hiddenLabel, selectedAttribute, winningAttribute }: Props) {
  const messages = useMessages();

  if (!card || isHidden) {
    const defaultHiddenLabel = side === 'cpu' ? messages.trunfo.cpuCard : messages.trunfo.hiddenCard;

    return (
      <article className="trunfo-card trunfo-card--back trunfo-card--face-down">
        <div className="trunfo-card-back-symbol">
          <ShieldQuestion size={42} />
        </div>
        <strong>{hiddenLabel ?? defaultHiddenLabel}</strong>
        <span>{messages.trunfo.revealHint}</span>
      </article>
    );
  }

  const theme = getTypeTheme(card.summary.types);
  const stateClass = side === 'cpu' && selectedAttribute ? 'trunfo-card--face-up' : '';
  const winnerClass = winningAttribute ? 'trunfo-card--round-winner' : '';
  const bestAttribute = ATTRIBUTE_OPTIONS
    .filter((option) => option.key !== 'total')
    .sort((a, b) => card.attributes[b.key] - card.attributes[a.key])[0];

  return (
    <article
      className={['trunfo-card', `trunfo-card--${card.rarity}`, stateClass, winnerClass].filter(Boolean).join(' ')}
      style={{ '--card-gradient': theme.gradient, '--type-accent': theme.accent } as CSSProperties}
    >
      <header className="trunfo-card-header">
        <span>{formatPokemonNumber(card.id)}</span>
        <strong><Sparkles size={14} /> {getRarityLabel(card.rarity, messages)}</strong>
      </header>

      <div className="trunfo-card-image">
        {card.rarity === 'lendaria' && <Crown className="trunfo-crown" size={26} />}
        <img src={assetUrl(card.summary.imageUrl)} alt={card.summary.name} loading="eager" decoding="async" />
      </div>

      <div className="trunfo-card-title">
        <h3>{formatPokemonName(card.summary.name)}</h3>
        <div className="type-list">
          {card.summary.types.map((type) => <TypeBadge key={type} type={type} label={messages.types[type as keyof typeof messages.types]} compact />)}
        </div>
      </div>

      <div className="trunfo-card-power">
        <span>{messages.trunfo.cardPower}</span>
        <strong>{card.attributes.total}</strong>
        {bestAttribute && <small>{messages.trunfo.bestAttribute}: {getAttributeShortLabel(bestAttribute.key, messages)}</small>}
      </div>

      <div className="trunfo-attribute-list">
        {ATTRIBUTE_OPTIONS.map((option) => {
          const isSelected = selectedAttribute === option.key;
          const isWinner = winningAttribute === option.key;

          return (
            <div
              className={[
                'trunfo-attribute-row',
                isSelected ? 'trunfo-attribute-row--selected' : '',
                isWinner ? 'trunfo-attribute-row--winner' : ''
              ].filter(Boolean).join(' ')}
              key={option.key}
            >
              <span>{getAttributeShortLabel(option.key, messages)}</span>
              <strong>{formatAttributeValue(option.key, card.attributes[option.key])}</strong>
            </div>
          );
        })}
      </div>

      {card.legendaryCharge && (
        <div className="trunfo-legendary-rule">
          <HelpCircle size={15} /> {messages.trunfo.legendaryRule}
        </div>
      )}
    </article>
  );
}

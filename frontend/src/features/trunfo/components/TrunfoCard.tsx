import { Crown, HelpCircle, ShieldQuestion, Sparkles } from 'lucide-react';
import type { CSSProperties } from 'react';
import { TypeBadge } from '../../../shared/components/TypeBadge';
import { assetUrl } from '../../../shared/utils/assets';
import { formatPokemonName, formatPokemonNumber } from '../../../shared/utils/format';
import { getTypeTheme } from '../../../shared/utils/typeTheme';
import type { TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfo';
import { ATTRIBUTE_OPTIONS, formatAttributeValue, getRarityLabel } from '../utils/trunfoRules';

type Props = {
  card: TrunfoCardModel | null;
  side: 'player' | 'cpu';
  isHidden?: boolean;
  selectedAttribute?: TrunfoAttributeKey | null;
  winningAttribute?: TrunfoAttributeKey | null;
};

export function TrunfoCard({ card, side, isHidden = false, selectedAttribute, winningAttribute }: Props) {
  if (!card || isHidden) {
    return (
      <article className="trunfo-card trunfo-card--back">
        <div className="trunfo-card-back-symbol">
          <ShieldQuestion size={42} />
        </div>
        <strong>{side === 'cpu' ? 'Carta da CPU' : 'Carta oculta'}</strong>
        <span>Escolha um atributo para revelar o duelo.</span>
      </article>
    );
  }

  const theme = getTypeTheme(card.summary.types);

  return (
    <article
      className={`trunfo-card trunfo-card--${card.rarity}`}
      style={{ '--card-gradient': theme.gradient, '--type-accent': theme.accent } as CSSProperties}
    >
      <header className="trunfo-card-header">
        <span>{formatPokemonNumber(card.id)}</span>
        <strong><Sparkles size={14} /> {getRarityLabel(card.rarity)}</strong>
      </header>

      <div className="trunfo-card-image">
        {card.rarity === 'lendaria' && <Crown className="trunfo-crown" size={26} />}
        <img src={assetUrl(card.summary.imageUrl)} alt={card.summary.name} loading="eager" decoding="async" />
      </div>

      <div className="trunfo-card-title">
        <h3>{formatPokemonName(card.summary.name)}</h3>
        <div className="type-list">
          {card.summary.types.map((type) => <TypeBadge key={type} type={type} compact />)}
        </div>
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
              <span>{option.shortLabel}</span>
              <strong>{formatAttributeValue(option.key, card.attributes[option.key])}</strong>
            </div>
          );
        })}
      </div>

      {card.legendaryCharge && (
        <div className="trunfo-legendary-rule">
          <HelpCircle size={15} /> vence empate uma vez
        </div>
      )}
    </article>
  );
}

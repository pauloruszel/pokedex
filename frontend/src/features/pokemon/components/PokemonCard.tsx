import { Heart, Plus, Search } from 'lucide-react';
import type { CSSProperties, MouseEvent } from 'react';
import { TypeBadge } from '../../../shared/components/TypeBadge';
import { assetUrl } from '../../../shared/utils/assets';
import { formatPokemonName, formatPokemonNumber } from '../../../shared/utils/format';
import { getTypeTheme } from '../../../shared/utils/typeTheme';
import type { PokemonSummary } from '../types/pokemon';

type Props = {
  pokemon: PokemonSummary;
  isFavorite: boolean;
  isCompared: boolean;
  onOpen: (pokemon: PokemonSummary) => void;
  onPrefetch: (pokemon: PokemonSummary) => void;
  onToggleFavorite: (pokemon: PokemonSummary) => void;
  onToggleCompare: (pokemon: PokemonSummary) => void;
};

export function PokemonCard({ pokemon, isFavorite, isCompared, onOpen, onPrefetch, onToggleFavorite, onToggleCompare }: Props) {
  const theme = getTypeTheme(pokemon.types);

  function handleFavorite(event: MouseEvent<HTMLButtonElement>) {
    event.stopPropagation();
    onToggleFavorite(pokemon);
  }

  function handleCompare(event: MouseEvent<HTMLButtonElement>) {
    event.stopPropagation();
    onToggleCompare(pokemon);
  }

  return (
    <article
      className="pokemon-card"
      style={{ '--card-gradient': theme.gradient, '--type-accent': theme.accent } as CSSProperties}
      onMouseEnter={() => onPrefetch(pokemon)}
      onFocus={() => onPrefetch(pokemon)}
    >
      <button className="card-hit-area" onClick={() => onOpen(pokemon)} aria-label={`Abrir detalhes de ${pokemon.name}`} />
      <span className="card-number">{formatPokemonNumber(pokemon.id)}</span>
      <span className="card-watermark">{String(pokemon.id).padStart(3, '0')}</span>

      <div className="card-actions">
        <button className={isFavorite ? 'icon-button icon-button--active' : 'icon-button'} onClick={handleFavorite} aria-label="Favoritar">
          <Heart size={17} fill={isFavorite ? 'currentColor' : 'none'} />
        </button>
        <button className={isCompared ? 'icon-button icon-button--active' : 'icon-button'} onClick={handleCompare} aria-label="Comparar">
          <Plus size={17} />
        </button>
      </div>

      <div className="pokemon-image-shell">
        <img src={assetUrl(pokemon.imageUrl)} alt={pokemon.name} loading="lazy" decoding="async" />
      </div>

      <div className="card-content">
        <h3>{formatPokemonName(pokemon.name)}</h3>
        <div className="type-list">
          {pokemon.types.map((type) => <TypeBadge type={type} compact key={type} />)}
        </div>
      </div>

      <button className="inspect-button" onClick={() => onOpen(pokemon)}>
        <Search size={15} /> Ver dossiê
      </button>
    </article>
  );
}

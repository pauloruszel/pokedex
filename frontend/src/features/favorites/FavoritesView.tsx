import { Heart, Trash2 } from 'lucide-react';
import { ptBR } from '../../shared/i18n/ptBR';
import { PokemonCard } from '../pokemon/components/PokemonCard';
import type { PokemonSummary } from '../pokemon/types/pokemon';

type Props = {
  favorites: PokemonSummary[];
  compareIds: number[];
  onOpen: (pokemon: PokemonSummary) => void;
  onPrefetch: (pokemon: PokemonSummary) => void;
  onToggleFavorite: (pokemon: PokemonSummary) => void;
  onToggleCompare: (pokemon: PokemonSummary) => void;
  onClearFavorites: () => void;
};

export function FavoritesView({ favorites, compareIds, onOpen, onPrefetch, onToggleFavorite, onToggleCompare, onClearFavorites }: Props) {
  if (favorites.length === 0) {
    return (
      <section className="empty-state">
        <Heart size={38} />
        <h2>{ptBR.favorites.emptyTitle}</h2>
        <p>{ptBR.favorites.emptyDescription}</p>
      </section>
    );
  }

  return (
    <>
      <section className="section-heading">
        <div>
          <span className="eyebrow-line">Coleção local</span>
          <h2>Favoritos</h2>
        </div>
        <button className="secondary-control" onClick={onClearFavorites}>
          <Trash2 size={17} /> {ptBR.favorites.clear}
        </button>
      </section>

      <section className="pokemon-grid">
        {favorites.map((pokemon) => (
          <PokemonCard
            key={pokemon.id}
            pokemon={pokemon}
            isFavorite
            isCompared={compareIds.includes(pokemon.id)}
            onOpen={onOpen}
            onPrefetch={onPrefetch}
            onToggleFavorite={onToggleFavorite}
            onToggleCompare={onToggleCompare}
          />
        ))}
      </section>
    </>
  );
}

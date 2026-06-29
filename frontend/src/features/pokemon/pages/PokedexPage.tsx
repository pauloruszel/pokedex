import { ChevronLeft, ChevronRight, SearchX } from 'lucide-react';
import { PokemonGridSkeleton } from '../../../shared/components/Skeleton';
import { useI18n } from '../../../shared/i18n/I18nProvider';
import { PokemonCard } from '../components/PokemonCard';
import { SearchCommand } from '../components/SearchCommand';
import type { PokemonSummary } from '../types/pokemon';

type Props = {
  pokemons: PokemonSummary[];
  types: string[];
  selectedType: string;
  search: string;
  total: number;
  offset: number;
  pageSize: number;
  isLoading: boolean;
  error: string | null;
  favoriteIds: number[];
  compareIds: number[];
  onSearchChange: (value: string) => void;
  onSearchSubmit: () => void;
  onClearSearch: () => void;
  onTypeChange: (value: string) => void;
  onPageChange: (offset: number) => void;
  onOpen: (pokemon: PokemonSummary) => void;
  onPrefetch: (pokemon: PokemonSummary) => void;
  onToggleFavorite: (pokemon: PokemonSummary) => void;
  onToggleCompare: (pokemon: PokemonSummary) => void;
};

export function PokedexPage({
  pokemons,
  types,
  selectedType,
  search,
  total,
  offset,
  pageSize,
  isLoading,
  error,
  favoriteIds,
  compareIds,
  onSearchChange,
  onSearchSubmit,
  onClearSearch,
  onTypeChange,
  onPageChange,
  onOpen,
  onPrefetch,
  onToggleFavorite,
  onToggleCompare
}: Props) {
  const { messages } = useI18n();
  const currentPage = Math.floor(offset / pageSize) + 1;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <>
      <SearchCommand
        search={search}
        selectedType={selectedType}
        types={types}
        onSearchChange={onSearchChange}
        onSubmit={onSearchSubmit}
        onClear={onClearSearch}
        onTypeChange={onTypeChange}
      />

      <section className="section-heading">
        <div>
          <span className="eyebrow-line">{messages.pokedex.eyebrow}</span>
          <h2>{messages.pokedex.title}</h2>
        </div>
        <strong>{total} {messages.pokedex.records}</strong>
      </section>

      {error && <div className="error-box">{error}</div>}

      {isLoading ? (
        <PokemonGridSkeleton />
      ) : pokemons.length === 0 ? (
        <section className="empty-state">
          <SearchX size={38} />
          <h2>{messages.pokedex.emptyTitle}</h2>
          <p>{messages.pokedex.emptyDescription}</p>
        </section>
      ) : (
        <section className="pokemon-grid">
          {pokemons.map((pokemon) => (
            <PokemonCard
              key={pokemon.id}
              pokemon={pokemon}
              isFavorite={favoriteIds.includes(pokemon.id)}
              isCompared={compareIds.includes(pokemon.id)}
              onOpen={onOpen}
              onPrefetch={onPrefetch}
              onToggleFavorite={onToggleFavorite}
              onToggleCompare={onToggleCompare}
            />
          ))}
        </section>
      )}

      <footer className="pagination">
        <button className="secondary-control" disabled={offset <= 0 || isLoading} onClick={() => onPageChange(Math.max(0, offset - pageSize))}>
          <ChevronLeft size={18} /> {messages.pokedex.previous}
        </button>
        <span>{messages.pokedex.page} {currentPage} {messages.pokedex.of} {totalPages}</span>
        <button className="secondary-control" disabled={offset + pageSize >= total || isLoading} onClick={() => onPageChange(offset + pageSize)}>
          {messages.pokedex.next} <ChevronRight size={18} />
        </button>
      </footer>
    </>
  );
}

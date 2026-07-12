import { CompareView } from '../features/compare/CompareView';
import { FavoritesView } from '../features/favorites/FavoritesView';
import { PokemonDetailDrawer } from '../features/pokemon/components/PokemonDetailDrawer';
import type { usePokemonExplorer } from '../features/pokemon/hooks/usePokemonExplorer';
import { PokedexPage } from '../features/pokemon/pages/PokedexPage';
import { TrunfoPage } from '../features/trunfo/pages/TrunfoPage';
import type { AppView } from './AppChrome';

type Props = {
  view: AppView;
  explorer: ReturnType<typeof usePokemonExplorer>;
};

export function AppRoutes({ view, explorer }: Props) {
  return (
    <>
      {view === 'pokedex' && (
        <PokedexPage
          pokemons={explorer.pokemons}
          types={explorer.types}
          selectedType={explorer.selectedType}
          search={explorer.search}
          total={explorer.total}
          offset={explorer.offset}
          pageSize={explorer.pageSize}
          isLoading={explorer.isLoading}
          error={explorer.error}
          favoriteIds={explorer.favoriteIds}
          compareIds={explorer.compareIds}
          onSearchChange={explorer.setSearch}
          onSearchSubmit={explorer.submitSearch}
          onClearSearch={explorer.clearSearch}
          onTypeChange={explorer.changeType}
          onPageChange={(nextOffset) => explorer.loadPokemons(nextOffset, explorer.selectedType)}
          onOpen={explorer.openDetail}
          onPrefetch={explorer.prefetchDetail}
          onToggleFavorite={explorer.toggleFavorite}
          onToggleCompare={explorer.toggleCompare}
        />
      )}

      {view === 'favorites' && (
        <FavoritesView
          favorites={explorer.favorites}
          compareIds={explorer.compareIds}
          onOpen={explorer.openDetail}
          onPrefetch={explorer.prefetchDetail}
          onToggleFavorite={explorer.toggleFavorite}
          onToggleCompare={explorer.toggleCompare}
          onClearFavorites={explorer.clearFavorites}
        />
      )}

      {view === 'compare' && (
        <CompareView
          compareSelection={explorer.compareSelection}
          compareDetails={explorer.compareDetails}
          isLoading={explorer.isCompareLoading}
          onClear={explorer.clearCompare}
          onOpen={explorer.openDetail}
        />
      )}

      {view === 'trunfo' && (
        <TrunfoPage
          favorites={explorer.favorites}
          types={explorer.types}
          loadDetail={explorer.loadDetail}
        />
      )}

      <PokemonDetailDrawer
        pokemon={explorer.detail}
        isLoading={explorer.isDetailLoading}
        onClose={explorer.closeDetail}
      />
    </>
  );
}

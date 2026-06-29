import { Database, Heart, LayoutGrid, Moon, Swords, Trophy } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { CompareView } from '../features/compare/CompareView';
import { FavoritesView } from '../features/favorites/FavoritesView';
import { pokemonApi } from '../features/pokemon/api/pokemonApi';
import { PokemonDetailDrawer } from '../features/pokemon/components/PokemonDetailDrawer';
import { PokedexPage } from '../features/pokemon/pages/PokedexPage';
import type { PokemonDetail, PokemonSummary } from '../features/pokemon/types/pokemon';
import { TrunfoPage } from '../features/trunfo/pages/TrunfoPage';
import type { TrunfoSetup } from '../features/trunfo/types/trunfo';
import { useDebounce } from '../shared/hooks/useDebounce';
import { useLocalStorage } from '../shared/hooks/useLocalStorage';
import { LanguageSwitcher } from '../shared/i18n/LanguageSwitcher';
import { useI18n } from '../shared/i18n/I18nProvider';
import '../styles/global.css';

const PAGE_SIZE = 24;
type View = 'pokedex' | 'favorites' | 'compare' | 'trunfo';

export default function App() {
  const { messages } = useI18n();
  const [view, setView] = useState<View>('pokedex');
  const [pokemons, setPokemons] = useState<PokemonSummary[]>([]);
  const [types, setTypes] = useState<string[]>([]);
  const [selectedType, setSelectedType] = useState('all');
  const [offset, setOffset] = useState(0);
  const [total, setTotal] = useState(0);
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search, 250);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [detail, setDetail] = useState<PokemonDetail | null>(null);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [compareDetails, setCompareDetails] = useState<PokemonDetail[]>([]);
  const [isCompareLoading, setIsCompareLoading] = useState(false);
  const [favorites, setFavorites] = useLocalStorage<PokemonSummary[]>('pokedex:favorites', []);
  const [compareSelection, setCompareSelection] = useLocalStorage<PokemonSummary[]>('pokedex:compare', []);
  const detailCache = useRef(new Map<number, PokemonDetail>());
  const detailRequests = useRef(new Map<number, Promise<PokemonDetail>>());

  const favoriteIds = useMemo(() => favorites.map((pokemon) => pokemon.id), [favorites]);
  const compareIds = useMemo(() => compareSelection.map((pokemon) => pokemon.id), [compareSelection]);

  useEffect(() => {
    pokemonApi.types().then(setTypes).catch(() => setTypes([]));
  }, []);

  useEffect(() => {
    loadPokemons(0, selectedType);
  }, [selectedType]);

  useEffect(() => {
    if (!debouncedSearch.trim() && pokemons.length === 1 && total === 1) {
      loadPokemons(0, selectedType);
    }
  }, [debouncedSearch]);

  useEffect(() => {
    if (isLoading || pokemons.length === 0) {
      return;
    }

    const preload = () => pokemons.slice(0, 6).forEach(prefetchDetail);
    const idleId = window.requestIdleCallback?.(preload, { timeout: 2500 });

    if (!idleId) {
      const timeoutId = window.setTimeout(preload, 700);
      return () => window.clearTimeout(timeoutId);
    }

    return () => window.cancelIdleCallback?.(idleId);
  }, [isLoading, pokemons]);

  useEffect(() => {
    if (compareSelection.length !== 2) {
      setCompareDetails([]);
      return;
    }

    let isActive = true;
    setIsCompareLoading(true);
    Promise.all(compareSelection.map((pokemon) => pokemonApi.detail(pokemon.id)))
      .then((details) => {
        if (isActive) setCompareDetails(details);
      })
      .catch(() => {
        if (isActive) setCompareDetails([]);
      })
      .finally(() => {
        if (isActive) setIsCompareLoading(false);
      });

    return () => {
      isActive = false;
    };
  }, [compareSelection]);

  async function loadPokemons(nextOffset = offset, type = selectedType) {
    setIsLoading(true);
    setError(null);

    try {
      const data = type === 'all'
        ? await pokemonApi.list(PAGE_SIZE, nextOffset)
        : await pokemonApi.byType(type, PAGE_SIZE, nextOffset);

      setPokemons(data.results);
      setOffset(data.offset);
      setTotal(data.count);
    } catch {
      setError(messages.errors.loadPokedex);
    } finally {
      setIsLoading(false);
    }
  }

  async function submitSearch() {
    if (!search.trim()) {
      await loadPokemons(0, selectedType);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const pokemon = await pokemonApi.search(search.trim());
      setPokemons([{ id: pokemon.id, name: pokemon.name, imageUrl: pokemon.imageUrl, types: pokemon.types }]);
      setTotal(1);
      setOffset(0);
    } catch {
      setError(messages.errors.notFound);
      setPokemons([]);
      setTotal(0);
    } finally {
      setIsLoading(false);
    }
  }

  async function openDetail(pokemon: PokemonSummary) {
    const cached = detailCache.current.get(pokemon.id);
    setDetail(cached ?? summaryToPreview(pokemon));
    setIsDetailLoading(!cached);

    if (cached) {
      return;
    }

    try {
      const loaded = await loadDetail(pokemon);
      setDetail(loaded);
    } finally {
      setIsDetailLoading(false);
    }
  }

  function prefetchDetail(pokemon: PokemonSummary) {
    if (detailCache.current.has(pokemon.id) || detailRequests.current.has(pokemon.id)) {
      return;
    }
    loadDetail(pokemon).catch(() => undefined);
  }

  function loadDetail(pokemon: PokemonSummary) {
    const existing = detailRequests.current.get(pokemon.id);
    if (existing) {
      return existing;
    }

    const request = pokemonApi.detail(pokemon.id)
      .then((loaded) => {
        detailCache.current.set(pokemon.id, loaded);
        return loaded;
      })
      .finally(() => {
        detailRequests.current.delete(pokemon.id);
      });

    detailRequests.current.set(pokemon.id, request);
    return request;
  }

  async function loadTrunfoCandidates(setup: TrunfoSetup) {
    if (setup.mode === 'type') {
      const data = await pokemonApi.byType(setup.type, 80, 0);
      return data.results;
    }

    const maxOffset = Math.max(0, total > 100 ? total - 80 : 945);
    const randomOffset = Math.floor(Math.random() * Math.max(1, maxOffset / 40)) * 40;
    const data = await pokemonApi.list(80, randomOffset);
    return data.results;
  }

  function toggleFavorite(pokemon: PokemonSummary) {
    setFavorites((current) => current.some((item) => item.id === pokemon.id)
      ? current.filter((item) => item.id !== pokemon.id)
      : [...current, pokemon]);
  }

  function toggleCompare(pokemon: PokemonSummary) {
    setCompareSelection((current) => {
      if (current.some((item) => item.id === pokemon.id)) {
        return current.filter((item) => item.id !== pokemon.id);
      }
      return [...current.slice(-1), pokemon];
    });
  }

  function clearSearch() {
    setSearch('');
    loadPokemons(0, selectedType);
  }

  return (
    <main className="app-shell">
      <section className={view === 'trunfo' ? 'hero-panel hero-panel--game' : 'hero-panel'}>
        <div className="hero-orb hero-orb--one" />
        <div className="hero-orb hero-orb--two" />
        <nav className="top-nav">
          <div className="brand-mark"><Database size={22} /> {messages.hero.brand}</div>
          <div className="nav-actions">
            <button className={view === 'pokedex' ? 'nav-pill nav-pill--active' : 'nav-pill'} onClick={() => setView('pokedex')}>
              <LayoutGrid size={16} /> {messages.nav.explore}
            </button>
            <button className={view === 'favorites' ? 'nav-pill nav-pill--active' : 'nav-pill'} onClick={() => setView('favorites')}>
              <Heart size={16} /> {messages.nav.favorites} <span>{favorites.length}</span>
            </button>
            <button className={view === 'compare' ? 'nav-pill nav-pill--active' : 'nav-pill'} onClick={() => setView('compare')}>
              <Swords size={16} /> {messages.nav.compare} <span>{compareSelection.length}/2</span>
            </button>
            <button className={view === 'trunfo' ? 'nav-pill nav-pill--active' : 'nav-pill'} onClick={() => setView('trunfo')}>
              <Trophy size={16} /> {messages.nav.trunfo}
            </button>
            <LanguageSwitcher />
          </div>
        </nav>

        {view === 'trunfo' && (
          <div className="hero-content hero-content--trunfo">
            <span className="eyebrow-line"><Trophy size={16} /> {messages.hero.trunfoEyebrow}</span>
            <h1>{messages.hero.trunfoTitle}</h1>
            <p>
              {messages.hero.trunfoDescription}
            </p>
          </div>
        )}

        <div className={view === 'trunfo' ? 'hero-content hero-content--hidden' : 'hero-content'}>
          <span className="eyebrow-line"><Moon size={16} /> {messages.hero.eyebrow}</span>
          <h1>{messages.hero.title}</h1>
          <p>
            {messages.hero.description}
          </p>
        </div>
      </section>

      {view === 'pokedex' && (
        <PokedexPage
          pokemons={pokemons}
          types={types}
          selectedType={selectedType}
          search={search}
          total={total}
          offset={offset}
          pageSize={PAGE_SIZE}
          isLoading={isLoading}
          error={error}
          favoriteIds={favoriteIds}
          compareIds={compareIds}
          onSearchChange={setSearch}
          onSearchSubmit={submitSearch}
          onClearSearch={clearSearch}
          onTypeChange={(type) => {
            setSelectedType(type);
            setSearch('');
          }}
          onPageChange={(nextOffset) => loadPokemons(nextOffset, selectedType)}
          onOpen={openDetail}
          onPrefetch={prefetchDetail}
          onToggleFavorite={toggleFavorite}
          onToggleCompare={toggleCompare}
        />
      )}

      {view === 'favorites' && (
        <FavoritesView
          favorites={favorites}
          compareIds={compareIds}
          onOpen={openDetail}
          onPrefetch={prefetchDetail}
          onToggleFavorite={toggleFavorite}
          onToggleCompare={toggleCompare}
          onClearFavorites={() => setFavorites([])}
        />
      )}

      {view === 'compare' && (
        <CompareView
          compareSelection={compareSelection}
          compareDetails={compareDetails}
          isLoading={isCompareLoading}
          onClear={() => setCompareSelection([])}
          onOpen={openDetail}
        />
      )}

      {view === 'trunfo' && (
        <TrunfoPage
          favorites={favorites}
          types={types}
          getCandidates={loadTrunfoCandidates}
          loadDetail={loadDetail}
        />
      )}

      {compareSelection.length > 0 && view !== 'compare' && (
        <button className="compare-floating" onClick={() => setView('compare')}>
          <Swords size={18} /> {messages.nav.compare} {compareSelection.length}/2
        </button>
      )}

      <PokemonDetailDrawer pokemon={detail} isLoading={isDetailLoading} onClose={() => setDetail(null)} />
    </main>
  );
}

function summaryToPreview(pokemon: PokemonSummary): PokemonDetail {
  return {
    ...pokemon,
    spriteUrl: null,
    height: 0,
    weight: 0,
    abilities: [],
    stats: [],
    species: {},
    evolutionChain: []
  };
}

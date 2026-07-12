import { useEffect, useMemo, useRef, useState } from 'react';
import { useDebounce } from '../../../shared/hooks/useDebounce';
import { useLocalStorage } from '../../../shared/hooks/useLocalStorage';
import type { AppLanguage } from '../../../shared/i18n/language';
import { pokemonApi } from '../api/pokemonApi';
import type { PokemonDetail, PokemonSummary } from '../types/pokemon';

const PAGE_SIZE = 24;

export function usePokemonExplorer(language: AppLanguage, errors: { loadPokedex: string; notFound: string }) {
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
  const [selectedDetail, setSelectedDetail] = useState<PokemonSummary | null>(null);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [compareDetails, setCompareDetails] = useState<PokemonDetail[]>([]);
  const [isCompareLoading, setIsCompareLoading] = useState(false);
  const [favorites, setFavorites] = useLocalStorage<PokemonSummary[]>('pokedex:favorites', []);
  const [compareSelection, setCompareSelection] = useLocalStorage<PokemonSummary[]>('pokedex:compare', []);
  const detailCache = useRef(new Map<string, PokemonDetail>());
  const detailRequests = useRef(new Map<string, Promise<PokemonDetail>>());

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
    Promise.all(compareSelection.map((pokemon) => pokemonApi.detail(pokemon.id, language)))
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
  }, [compareSelection, language]);

  useEffect(() => {
    if (selectedDetail) {
      openDetail(selectedDetail);
    }
  }, [language]);

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
      setError(errors.loadPokedex);
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
      const pokemon = await pokemonApi.search(search.trim(), language);
      setPokemons([{ id: pokemon.id, name: pokemon.name, imageUrl: pokemon.imageUrl, types: pokemon.types }]);
      setTotal(1);
      setOffset(0);
    } catch {
      setError(errors.notFound);
      setPokemons([]);
      setTotal(0);
    } finally {
      setIsLoading(false);
    }
  }

  async function openDetail(pokemon: PokemonSummary) {
    setSelectedDetail(pokemon);
    const key = detailKey(pokemon.id, language);
    const cached = detailCache.current.get(key);
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
    const key = detailKey(pokemon.id, language);
    if (detailCache.current.has(key) || detailRequests.current.has(key)) {
      return;
    }
    loadDetail(pokemon).catch(() => undefined);
  }

  function loadDetail(pokemon: PokemonSummary) {
    const key = detailKey(pokemon.id, language);
    const existing = detailRequests.current.get(key);
    if (existing) {
      return existing;
    }

    const request = pokemonApi.detail(pokemon.id, language)
      .then((loaded) => {
        detailCache.current.set(key, loaded);
        return loaded;
      })
      .finally(() => {
        detailRequests.current.delete(key);
      });

    detailRequests.current.set(key, request);
    return request;
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

  function changeType(type: string) {
    setSelectedType(type);
    setSearch('');
  }

  function closeDetail() {
    setDetail(null);
    setSelectedDetail(null);
  }

  return {
    pageSize: PAGE_SIZE,
    pokemons,
    types,
    selectedType,
    search,
    total,
    offset,
    isLoading,
    error,
    detail,
    isDetailLoading,
    compareDetails,
    isCompareLoading,
    favorites,
    compareSelection,
    favoriteIds,
    compareIds,
    setSearch,
    submitSearch,
    clearSearch,
    changeType,
    loadPokemons,
    openDetail,
    prefetchDetail,
    loadDetail,
    toggleFavorite,
    toggleCompare,
    closeDetail,
    clearFavorites: () => setFavorites([]),
    clearCompare: () => setCompareSelection([])
  };
}

function detailKey(id: number, locale: string) {
  return `${id}:${locale}`;
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

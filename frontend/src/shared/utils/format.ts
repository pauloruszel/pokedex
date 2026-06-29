export function formatPokemonName(name: string) {
  return name
    .split('-')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

export function formatPokemonNumber(id: number) {
  return `#${String(id).padStart(4, '0')}`;
}

export function formatStatName(name: string) {
  const map: Record<string, string> = {
    hp: 'HP',
    attack: 'Ataque',
    defense: 'Defesa',
    'special-attack': 'Atq. Esp.',
    'special-defense': 'Def. Esp.',
    speed: 'Velocidade'
  };

  return map[name] ?? formatPokemonName(name);
}

export function formatAbilityName(name: string) {
  const map: Record<string, string> = {
    overgrow: 'Supercrescimento',
    blaze: 'Chama',
    torrent: 'Torrente',
    'shield-dust': 'Pó Escudo',
    'shed-skin': 'Troca de Pele',
    'compound-eyes': 'Olhos Compostos',
    swarm: 'Enxame',
    'keen-eye': 'Olhar Aguçado',
    'tangled-feet': 'Pés Emaranhados',
    guts: 'Coragem',
    'run-away': 'Fuga',
    static: 'Estática',
    'lightning-rod': 'Para-raios',
    chlorophyll: 'Clorofila',
    'poison-point': 'Ponto Venenoso',
    rivalry: 'Rivalidade',
    intimidate: 'Intimidação',
    'inner-focus': 'Foco Interno',
    levitate: 'Levitação',
    pressure: 'Pressão'
  };

  return map[name] ?? formatPokemonName(name);
}

export function formatHabitatName(name: string) {
  const map: Record<string, string> = {
    cave: 'Caverna',
    forest: 'Floresta',
    grassland: 'Campo',
    mountain: 'Montanha',
    rare: 'Raro',
    'rough-terrain': 'Terreno acidentado',
    sea: 'Mar',
    urban: 'Urbano',
    'waters-edge': 'Beira da água'
  };

  return map[name] ?? formatPokemonName(name);
}

export function formatGenerationName(name: string) {
  const match = name.match(/^generation-(.+)$/);
  return match ? `Geração ${match[1].toUpperCase()}` : formatPokemonName(name);
}

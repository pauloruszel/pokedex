const fallbackType = 'normal';

export const typeTheme: Record<string, { label: string; gradient: string; accent: string }> = {
  normal: { label: 'Normal', gradient: 'linear-gradient(135deg, #d8d5ca, #f7f4ea)', accent: '#8f8a7d' },
  fire: { label: 'Fogo', gradient: 'linear-gradient(135deg, #ff7147, #ffcb66)', accent: '#ff5a36' },
  water: { label: 'Água', gradient: 'linear-gradient(135deg, #3f8cff, #76d7ff)', accent: '#3488ff' },
  grass: { label: 'Grama', gradient: 'linear-gradient(135deg, #3fce7a, #b7f36a)', accent: '#2ebd68' },
  electric: { label: 'Elétrico', gradient: 'linear-gradient(135deg, #ffd23f, #fff27a)', accent: '#e3b600' },
  ice: { label: 'Gelo', gradient: 'linear-gradient(135deg, #7ce8ff, #d4fbff)', accent: '#3bbfd8' },
  fighting: { label: 'Lutador', gradient: 'linear-gradient(135deg, #d94c38, #ff9b74)', accent: '#bd3c2d' },
  poison: { label: 'Venenoso', gradient: 'linear-gradient(135deg, #9e5bff, #e199ff)', accent: '#8b48e8' },
  ground: { label: 'Terra', gradient: 'linear-gradient(135deg, #d2a45f, #f6dd9f)', accent: '#bb8743' },
  flying: { label: 'Voador', gradient: 'linear-gradient(135deg, #7ea7ff, #d9e7ff)', accent: '#658ff0' },
  psychic: { label: 'Psíquico', gradient: 'linear-gradient(135deg, #ff5da8, #ffc0de)', accent: '#f04696' },
  bug: { label: 'Inseto', gradient: 'linear-gradient(135deg, #9fca3d, #e4f48a)', accent: '#8daf2f' },
  rock: { label: 'Pedra', gradient: 'linear-gradient(135deg, #ad8b45, #dec78a)', accent: '#967437' },
  ghost: { label: 'Fantasma', gradient: 'linear-gradient(135deg, #6f65c8, #b5a8ff)', accent: '#6555c9' },
  dragon: { label: 'Dragão', gradient: 'linear-gradient(135deg, #5457d6, #ff8a6d)', accent: '#5351cf' },
  dark: { label: 'Sombrio', gradient: 'linear-gradient(135deg, #3d3945, #8f819b)', accent: '#312d38' },
  steel: { label: 'Aço', gradient: 'linear-gradient(135deg, #8da4b8, #dbe7ef)', accent: '#758fa5' },
  fairy: { label: 'Fada', gradient: 'linear-gradient(135deg, #ff8fcb, #ffe0f4)', accent: '#ee74b5' }
};

export function getTypeTheme(types: string[] | string | undefined) {
  const primaryType = Array.isArray(types) ? types[0] : types;
  return typeTheme[primaryType ?? fallbackType] ?? typeTheme[fallbackType];
}

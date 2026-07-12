import type { Messages } from '../../../shared/i18n/messages';
import type { TrunfoAttributeKey, TrunfoRarity } from '../types/trunfoCard';

type AttributeOption = {
  key: TrunfoAttributeKey;
  label: string;
  shortLabel: string;
  unit?: string;
};

export const ATTRIBUTE_OPTIONS: AttributeOption[] = [
  { key: 'hp', label: 'HP', shortLabel: 'HP' },
  { key: 'attack', label: 'Ataque', shortLabel: 'ATQ' },
  { key: 'defense', label: 'Defesa', shortLabel: 'DEF' },
  { key: 'specialAttack', label: 'Ataque especial', shortLabel: 'ATQ ESP' },
  { key: 'specialDefense', label: 'Defesa especial', shortLabel: 'DEF ESP' },
  { key: 'speed', label: 'Velocidade', shortLabel: 'VEL' },
  { key: 'weight', label: 'Peso', shortLabel: 'PESO', unit: 'kg' },
  { key: 'height', label: 'Altura', shortLabel: 'ALT', unit: 'm' },
  { key: 'total', label: 'Total de status', shortLabel: 'TOTAL' }
];

export function formatAttributeValue(attribute: TrunfoAttributeKey, value: number) {
  const option = ATTRIBUTE_OPTIONS.find((item) => item.key === attribute);

  if (attribute === 'height') return `${value.toFixed(1)} m`;
  if (attribute === 'weight') return `${value.toFixed(1)} kg`;

  return option?.unit ? `${value} ${option.unit}` : String(value);
}

export function getAttributeLabel(attribute: TrunfoAttributeKey, messages?: Messages) {
  if (messages) {
    const labels: Record<TrunfoAttributeKey, string> = {
      hp: messages.attributes.hp,
      attack: messages.attributes.attack,
      defense: messages.attributes.defense,
      specialAttack: messages.attributes.specialAttack,
      specialDefense: messages.attributes.specialDefense,
      speed: messages.attributes.speed,
      weight: messages.attributes.weight,
      height: messages.attributes.height,
      total: messages.attributes.total
    };
    return labels[attribute];
  }

  return ATTRIBUTE_OPTIONS.find((item) => item.key === attribute)?.label ?? attribute;
}

export function getAttributeShortLabel(attribute: TrunfoAttributeKey, messages?: Messages) {
  if (messages) {
    const labels: Record<TrunfoAttributeKey, string> = {
      hp: messages.attributes.hp,
      attack: messages.attributes.shortAttack,
      defense: messages.attributes.shortDefense,
      specialAttack: messages.attributes.shortSpecialAttack,
      specialDefense: messages.attributes.shortSpecialDefense,
      speed: messages.attributes.shortSpeed,
      weight: messages.attributes.shortWeight,
      height: messages.attributes.shortHeight,
      total: messages.attributes.total
    };
    return labels[attribute];
  }

  return ATTRIBUTE_OPTIONS.find((item) => item.key === attribute)?.shortLabel ?? attribute;
}

export function getRarityLabel(rarity: TrunfoRarity, messages?: Messages) {
  const labels: Record<TrunfoRarity, string> = {
    comum: messages?.trunfo.rarityCommon ?? 'Comum',
    rara: messages?.trunfo.rarityRare ?? 'Rara',
    epica: messages?.trunfo.rarityEpic ?? 'Epica',
    lendaria: messages?.trunfo.rarityLegendary ?? 'Lendaria'
  };

  return labels[rarity];
}

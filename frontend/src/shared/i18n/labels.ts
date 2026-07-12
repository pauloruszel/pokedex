import type { Messages } from './messages';

export function statLabel(name: string, messages: Messages) {
  const map: Record<string, string> = {
    hp: messages.attributes.hp,
    attack: messages.attributes.attack,
    defense: messages.attributes.defense,
    'special-attack': messages.attributes.specialAttack,
    'special-defense': messages.attributes.specialDefense,
    speed: messages.attributes.speed
  };

  return map[name] ?? name;
}

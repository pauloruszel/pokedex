import { useI18n } from '../i18n/I18nProvider';

type Props = {
  name: string;
  value: number;
  max?: number;
};

export function StatBar({ name, value, max = 160 }: Props) {
  const { messages } = useI18n();
  const width = Math.max(4, Math.min(100, (value / max) * 100));
  const label = statLabel(name, messages);

  return (
    <div className="stat-row">
      <span>{label}</span>
      <div className="stat-track" aria-label={`${label} ${value}`}>
        <div className="stat-fill" style={{ width: `${width}%` }} />
      </div>
      <strong>{value}</strong>
    </div>
  );
}

function statLabel(name: string, messages: ReturnType<typeof useI18n>['messages']) {
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

import { formatStatName } from '../utils/format';

type Props = {
  name: string;
  value: number;
  max?: number;
};

export function StatBar({ name, value, max = 160 }: Props) {
  const width = Math.max(4, Math.min(100, (value / max) * 100));

  return (
    <div className="stat-row">
      <span>{formatStatName(name)}</span>
      <div className="stat-track" aria-label={`${formatStatName(name)} ${value}`}>
        <div className="stat-fill" style={{ width: `${width}%` }} />
      </div>
      <strong>{value}</strong>
    </div>
  );
}

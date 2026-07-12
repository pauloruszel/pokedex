type Props = {
  name: string;
  value: number;
  max?: number;
  label?: string;
};

export function StatBar({ name, value, max = 160, label = name }: Props) {
  const width = Math.max(4, Math.min(100, (value / max) * 100));

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

import type { CSSProperties } from 'react';
import { getTypeTheme } from '../utils/typeTheme';

type Props = {
  type: string;
  compact?: boolean;
  label?: string;
};

export function TypeBadge({ type, compact = false, label }: Props) {
  const theme = getTypeTheme(type);

  return (
    <span className={compact ? 'type-badge type-badge--compact' : 'type-badge'} style={{ '--type-accent': theme.accent } as CSSProperties}>
      {label ?? theme.label}
    </span>
  );
}

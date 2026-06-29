import type { CSSProperties } from 'react';
import { getTypeTheme } from '../utils/typeTheme';

type Props = {
  type: string;
  compact?: boolean;
};

export function TypeBadge({ type, compact = false }: Props) {
  const theme = getTypeTheme(type);
  return (
    <span className={compact ? 'type-badge type-badge--compact' : 'type-badge'} style={{ '--type-accent': theme.accent } as CSSProperties}>
      {theme.label}
    </span>
  );
}

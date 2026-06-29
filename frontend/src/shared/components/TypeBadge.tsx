import type { CSSProperties } from 'react';
import { useI18n } from '../i18n/I18nProvider';
import { getTypeTheme } from '../utils/typeTheme';

type Props = {
  type: string;
  compact?: boolean;
};

export function TypeBadge({ type, compact = false }: Props) {
  const { messages } = useI18n();
  const theme = getTypeTheme(type);
  const label = messages.types[type as keyof typeof messages.types] ?? theme.label;

  return (
    <span className={compact ? 'type-badge type-badge--compact' : 'type-badge'} style={{ '--type-accent': theme.accent } as CSSProperties}>
      {label}
    </span>
  );
}

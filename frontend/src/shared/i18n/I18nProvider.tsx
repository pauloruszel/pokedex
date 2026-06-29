import { createContext, useContext, useMemo, type ReactNode } from 'react';
import { useLocalStorage } from '../hooks/useLocalStorage';
import { getMessages, interpolate, type AppLanguage, type Messages } from './messages';

type I18nContextValue = {
  language: AppLanguage;
  setLanguage: (language: AppLanguage) => void;
  messages: Messages;
  format: (template: string, values: Record<string, string | number>) => string;
};

const I18nContext = createContext<I18nContextValue | null>(null);

export function I18nProvider({ children }: { children: ReactNode }) {
  const [language, setLanguage] = useLocalStorage<AppLanguage>('pokedex:language', 'pt-BR');

  const value = useMemo<I18nContextValue>(() => ({
    language,
    setLanguage,
    messages: getMessages(language),
    format: interpolate
  }), [language, setLanguage]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n() {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useI18n must be used inside I18nProvider');
  }
  return context;
}

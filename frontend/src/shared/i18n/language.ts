export type AppLanguage = 'pt-BR' | 'es' | 'en';

export const languageOptions: Array<{ code: AppLanguage; label: string; shortLabel: string; flag: string }> = [
  { code: 'pt-BR', label: 'Português', shortLabel: 'PT', flag: 'BR' },
  { code: 'es', label: 'Español', shortLabel: 'ES', flag: 'ES' },
  { code: 'en', label: 'English', shortLabel: 'EN', flag: 'US' }
];

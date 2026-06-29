import { API_BASE_URL } from '../utils/assets';
import type { AppLanguage } from './messages';

type TranslationResponse = {
  text: string;
  locale: AppLanguage;
  source: string;
};

export async function translateText(params: {
  text: string;
  sourceLocale?: AppLanguage;
  targetLocale: AppLanguage;
  kind: string;
  signal?: AbortSignal;
}) {
  const response = await fetch(`${API_BASE_URL}/api/i18n/translate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    signal: params.signal,
    body: JSON.stringify({
      text: params.text,
      sourceLocale: params.sourceLocale ?? 'pt-BR',
      targetLocale: params.targetLocale,
      kind: params.kind
    })
  });

  if (!response.ok) {
    throw new Error(`Translation failed: ${response.status}`);
  }

  return response.json() as Promise<TranslationResponse>;
}

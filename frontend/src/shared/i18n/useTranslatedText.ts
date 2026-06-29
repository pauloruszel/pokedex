import { useEffect, useMemo, useState } from 'react';
import { translateText } from './translationApi';
import { useI18n } from './I18nProvider';

const memoryCache = new Map<string, string>();

export function useTranslatedText(text: string | null | undefined, kind: string) {
  const { language } = useI18n();
  const sourceText = text?.trim() ?? '';
  const cacheKey = useMemo(() => `${language}|${kind}|${sourceText}`, [language, kind, sourceText]);
  const [translated, setTranslated] = useState(sourceText);
  const [isTranslating, setIsTranslating] = useState(false);

  useEffect(() => {
    if (!sourceText || language === 'pt-BR') {
      setTranslated(sourceText);
      setIsTranslating(false);
      return;
    }

    const cached = memoryCache.get(cacheKey);
    if (cached) {
      setTranslated(cached);
      setIsTranslating(false);
      return;
    }

    const controller = new AbortController();
    setIsTranslating(true);
    translateText({
      text: sourceText,
      sourceLocale: 'pt-BR',
      targetLocale: language,
      kind,
      signal: controller.signal
    })
      .then((result) => {
        memoryCache.set(cacheKey, result.text);
        setTranslated(result.text);
      })
      .catch(() => setTranslated(sourceText))
      .finally(() => setIsTranslating(false));

    return () => controller.abort();
  }, [cacheKey, kind, language, sourceText]);

  return { text: translated, isTranslating };
}

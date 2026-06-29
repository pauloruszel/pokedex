import { Languages } from 'lucide-react';
import { languageOptions } from './messages';
import { useI18n } from './I18nProvider';

export function LanguageSwitcher() {
  const { language, setLanguage, messages } = useI18n();
  const current = languageOptions.find((option) => option.code === language) ?? languageOptions[0];

  return (
    <div className="language-switcher" aria-label={messages.language.label}>
      <Languages size={15} />
      {languageOptions.map((option) => (
        <button
          className={option.code === language ? 'language-option language-option--active' : 'language-option'}
          key={option.code}
          onClick={() => setLanguage(option.code)}
          title={`${messages.language.changeTo} ${option.label}`}
          type="button"
        >
          <span aria-hidden="true">{option.flag}</span>
          <strong>{option.shortLabel}</strong>
        </button>
      ))}
      <span className="language-current">{current.shortLabel}</span>
    </div>
  );
}

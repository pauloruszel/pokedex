import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './app/App';
import { I18nProvider } from './shared/i18n/I18nProvider';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <I18nProvider>
      <App />
    </I18nProvider>
  </StrictMode>
);

if ('serviceWorker' in navigator && import.meta.env.PROD) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => undefined);
  });
}

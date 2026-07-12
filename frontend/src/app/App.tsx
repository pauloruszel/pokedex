import { useState } from 'react';
import { usePokemonExplorer } from '../features/pokemon/hooks/usePokemonExplorer';
import { useAppLanguage, useMessages } from '../shared/i18n/I18nProvider';
import { AppChrome, type AppView } from './AppChrome';
import { AppRoutes } from './AppRoutes';
import '../styles/global.css';

export default function App() {
  const messages = useMessages();
  const { language } = useAppLanguage();
  const [view, setView] = useState<AppView>('pokedex');
  const explorer = usePokemonExplorer(language, messages.errors);

  return (
    <main className="app-shell">
      <AppChrome
        view={view}
        favoritesCount={explorer.favorites.length}
        compareCount={explorer.compareSelection.length}
        onViewChange={setView}
      />

      <AppRoutes view={view} explorer={explorer} />
    </main>
  );
}

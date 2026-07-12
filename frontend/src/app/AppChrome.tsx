import { Database, Heart, LayoutGrid, Moon, Swords, Trophy } from 'lucide-react';
import { useMessages } from '../shared/i18n/I18nProvider';
import { LanguageSwitcher } from '../shared/i18n/LanguageSwitcher';

export type AppView = 'pokedex' | 'favorites' | 'compare' | 'trunfo';

type Props = {
  view: AppView;
  favoritesCount: number;
  compareCount: number;
  onViewChange: (view: AppView) => void;
};

export function AppChrome({ view, favoritesCount, compareCount, onViewChange }: Props) {
  const messages = useMessages();

  return (
    <>
      <section className={view === 'trunfo' ? 'hero-panel hero-panel--game' : 'hero-panel'}>
        <div className="hero-orb hero-orb--one" />
        <div className="hero-orb hero-orb--two" />
        <nav className="top-nav">
          <div className="brand-mark"><Database size={22} /> {messages.hero.brand}</div>
          <div className="nav-actions">
            <button className={navClass(view, 'pokedex')} onClick={() => onViewChange('pokedex')}>
              <LayoutGrid size={16} /> {messages.nav.explore}
            </button>
            <button className={navClass(view, 'favorites')} onClick={() => onViewChange('favorites')}>
              <Heart size={16} /> {messages.nav.favorites} <span>{favoritesCount}</span>
            </button>
            <button className={navClass(view, 'compare')} onClick={() => onViewChange('compare')}>
              <Swords size={16} /> {messages.nav.compare} <span>{compareCount}/2</span>
            </button>
            <button className={navClass(view, 'trunfo')} onClick={() => onViewChange('trunfo')}>
              <Trophy size={16} /> {messages.nav.trunfo}
            </button>
            <LanguageSwitcher />
          </div>
        </nav>

        {view === 'trunfo' && (
          <div className="hero-content hero-content--trunfo">
            <span className="eyebrow-line"><Trophy size={16} /> {messages.hero.trunfoEyebrow}</span>
            <h1>{messages.hero.trunfoTitle}</h1>
            <p>{messages.hero.trunfoDescription}</p>
          </div>
        )}

        <div className={view === 'trunfo' ? 'hero-content hero-content--hidden' : 'hero-content'}>
          <span className="eyebrow-line"><Moon size={16} /> {messages.hero.eyebrow}</span>
          <h1>{messages.hero.title}</h1>
          <p>{messages.hero.description}</p>
        </div>
      </section>

      {compareCount > 0 && view !== 'compare' && (
        <button className="compare-floating" onClick={() => onViewChange('compare')}>
          <Swords size={18} /> {messages.nav.compare} {compareCount}/2
        </button>
      )}
    </>
  );
}

function navClass(current: AppView, target: AppView) {
  return current === target ? 'nav-pill nav-pill--active' : 'nav-pill';
}

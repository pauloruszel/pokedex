import type { CSSProperties, ReactNode } from 'react';
import { ArrowRight, Dna, ImageIcon, Ruler, Scale, Sparkles, X } from 'lucide-react';
import { TypeBadge } from '../../../shared/components/TypeBadge';
import { StatBar } from '../../../shared/components/StatBar';
import { useMessages } from '../../../shared/i18n/I18nProvider';
import { statLabel } from '../../../shared/i18n/labels';
import { useTranslatedText } from '../../../shared/i18n/useTranslatedText';
import { assetUrl } from '../../../shared/utils/assets';
import { formatAbilityName, formatGenerationName, formatHabitatName, formatPokemonName, formatPokemonNumber } from '../../../shared/utils/format';
import { getTypeTheme } from '../../../shared/utils/typeTheme';
import type { PokemonDetail } from '../types/pokemon';

type Props = {
  pokemon: PokemonDetail | null;
  isLoading: boolean;
  onClose: () => void;
};

export function PokemonDetailDrawer({ pokemon, isLoading, onClose }: Props) {
  const messages = useMessages();
  const generationText = useTranslatedText(
    pokemon?.species?.generation ? formatGenerationName(pokemon.species.generation) : null,
    'pokemon_generation'
  );
  const habitatText = useTranslatedText(
    pokemon?.species?.habitat ? formatHabitatName(pokemon.species.habitat) : null,
    'pokemon_habitat'
  );

  if (!pokemon && !isLoading) return null;

  const theme = getTypeTheme(pokemon?.types);
  const hasFullDetail = Boolean(pokemon && pokemon.height > 0 && pokemon.weight > 0);

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <aside className="detail-drawer" onClick={(event) => event.stopPropagation()} style={{ '--detail-gradient': theme.gradient, '--type-accent': theme.accent } as CSSProperties}>
        <button className="drawer-close" onClick={onClose} aria-label={messages.detail.close}>
          <X size={22} />
        </button>

        {!pokemon ? (
          <div className="drawer-loading">
            <div className="pokeball-loader" />
            <strong>{messages.detail.loading}</strong>
          </div>
        ) : (
          <>
            <header className="detail-hero">
              <div className="detail-hero-copy">
                <span className="detail-number">{formatPokemonNumber(pokemon.id)}</span>
                <h2>{formatPokemonName(pokemon.name)}</h2>
                <p>{pokemon.species?.genus || messages.detail.fallbackGenus}</p>
                <div className="type-list type-list--left">
                  {pokemon.types.map((type) => <TypeBadge type={type} label={messages.types[type as keyof typeof messages.types]} key={type} />)}
                </div>
              </div>

              <div className="detail-image-orbit">
                <span />
                <img src={assetUrl(pokemon.imageUrl)} alt={pokemon.name} decoding="async" />
              </div>
            </header>

            <section className="flavor-panel">
              <Sparkles size={18} />
              <p>{pokemon.species?.flavorText || (isLoading ? messages.detail.translating : messages.detail.noDescription)}</p>
            </section>

            {hasFullDetail ? <section className="metric-grid">
              <Metric icon={<Ruler size={18} />} label={messages.detail.height} value={`${(pokemon.height / 10).toFixed(1)} m`} />
              <Metric icon={<Scale size={18} />} label={messages.detail.weight} value={`${(pokemon.weight / 10).toFixed(1)} kg`} />
              <Metric icon={<Dna size={18} />} label={messages.detail.generation} value={generationText.text || 'N/A'} />
              <Metric icon={<ImageIcon size={18} />} label={messages.detail.habitat} value={habitatText.text || 'N/A'} />
            </section> : <section className="detail-progress-panel"><div className="pokeball-loader pokeball-loader--small" /><span>{messages.detail.progress}</span></section>}

            {hasFullDetail && <div className="detail-two-columns">
              <section className="detail-card-panel">
                <h3>{messages.detail.stats}</h3>
                <div className="stats-list">
                  {pokemon.stats.map((stat) => <StatBar key={stat.name} name={stat.name} value={stat.value} label={statLabel(stat.name, messages)} />)}
                </div>
              </section>

              <section className="detail-card-panel">
                <h3>{messages.detail.abilities}</h3>
                <div className="ability-list">
                  {pokemon.abilities.map((ability) => <TranslatedAbility ability={ability} key={ability} />)}
                </div>

                <h3 className="section-spaced">{messages.detail.gallery}</h3>
                <div className="sprite-gallery">
                  <img src={assetUrl(pokemon.imageUrl)} alt={`${pokemon.name} official artwork`} decoding="async" />
                  {pokemon.spriteUrl && <img src={assetUrl(pokemon.spriteUrl)} alt={`${pokemon.name} sprite`} decoding="async" />}
                </div>
              </section>
            </div>}

            {hasFullDetail && <section className="detail-card-panel evolution-panel">
              <h3>{messages.detail.evolution}</h3>
              <div className="evolution-chain">
                {pokemon.evolutionChain.length > 0 ? pokemon.evolutionChain.map((name, index) => (
                  <div className="evolution-item" key={`${name}-${index}`}>
                    <span>{formatPokemonName(name)}</span>
                    {index < pokemon.evolutionChain.length - 1 && <ArrowRight size={18} />}
                  </div>
                )) : <span>{messages.detail.noEvolution}</span>}
              </div>
            </section>}
          </>
        )}
      </aside>
    </div>
  );
}

function TranslatedAbility({ ability }: { ability: string }) {
  const translated = useTranslatedText(formatAbilityName(ability), 'pokemon_ability');
  return <span>{translated.text}</span>;
}

function Metric({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="metric-card">
      <span>{icon}</span>
      <div>
        <small>{label}</small>
        <strong>{value}</strong>
      </div>
    </div>
  );
}

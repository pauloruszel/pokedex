import type { CSSProperties, ReactNode } from 'react';
import { ArrowRight, Dna, ImageIcon, Ruler, Scale, Sparkles, X } from 'lucide-react';
import { TypeBadge } from '../../../shared/components/TypeBadge';
import { StatBar } from '../../../shared/components/StatBar';
import { ptBR } from '../../../shared/i18n/ptBR';
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
  if (!pokemon && !isLoading) return null;

  const theme = getTypeTheme(pokemon?.types);
  const hasFullDetail = Boolean(pokemon && pokemon.height > 0 && pokemon.weight > 0);

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <aside className="detail-drawer" onClick={(event) => event.stopPropagation()} style={{ '--detail-gradient': theme.gradient, '--type-accent': theme.accent } as CSSProperties}>
        <button className="drawer-close" onClick={onClose} aria-label={ptBR.detail.close}>
          <X size={22} />
        </button>

        {!pokemon ? (
          <div className="drawer-loading">
            <div className="pokeball-loader" />
            <strong>{ptBR.detail.loading}</strong>
          </div>
        ) : (
          <>
            <header className="detail-hero">
              <div className="detail-hero-copy">
                <span className="detail-number">{formatPokemonNumber(pokemon.id)}</span>
                <h2>{formatPokemonName(pokemon.name)}</h2>
                <p>{pokemon.species?.genus ?? ptBR.detail.fallbackGenus}</p>
                <div className="type-list type-list--left">
                  {pokemon.types.map((type) => <TypeBadge type={type} key={type} />)}
                </div>
              </div>

              <div className="detail-image-orbit">
                <span />
                <img src={assetUrl(pokemon.imageUrl)} alt={pokemon.name} decoding="async" />
              </div>
            </header>

            <section className="flavor-panel">
              <Sparkles size={18} />
              <p>{pokemon.species?.flavorText ?? ptBR.detail.noDescription}</p>
            </section>

            {hasFullDetail ? <section className="metric-grid">
              <Metric icon={<Ruler size={18} />} label={ptBR.detail.height} value={`${(pokemon.height / 10).toFixed(1)} m`} />
              <Metric icon={<Scale size={18} />} label={ptBR.detail.weight} value={`${(pokemon.weight / 10).toFixed(1)} kg`} />
              <Metric icon={<Dna size={18} />} label={ptBR.detail.generation} value={formatGenerationName(pokemon.species?.generation ?? 'N/A')} />
              <Metric icon={<ImageIcon size={18} />} label={ptBR.detail.habitat} value={formatHabitatName(pokemon.species?.habitat ?? 'N/A')} />
            </section> : <section className="detail-progress-panel"><div className="pokeball-loader pokeball-loader--small" /><span>{ptBR.detail.progress}</span></section>}

            {hasFullDetail && <div className="detail-two-columns">
              <section className="detail-card-panel">
                <h3>{ptBR.detail.stats}</h3>
                <div className="stats-list">
                  {pokemon.stats.map((stat) => <StatBar key={stat.name} name={stat.name} value={stat.value} />)}
                </div>
              </section>

              <section className="detail-card-panel">
                <h3>{ptBR.detail.abilities}</h3>
                <div className="ability-list">
                  {pokemon.abilities.map((ability) => <span key={ability}>{formatAbilityName(ability)}</span>)}
                </div>

                <h3 className="section-spaced">{ptBR.detail.gallery}</h3>
                <div className="sprite-gallery">
                  <img src={assetUrl(pokemon.imageUrl)} alt={`${pokemon.name} official artwork`} decoding="async" />
                  {pokemon.spriteUrl && <img src={assetUrl(pokemon.spriteUrl)} alt={`${pokemon.name} sprite`} decoding="async" />}
                </div>
              </section>
            </div>}

            {hasFullDetail && <section className="detail-card-panel evolution-panel">
              <h3>{ptBR.detail.evolution}</h3>
              <div className="evolution-chain">
                {pokemon.evolutionChain.length > 0 ? pokemon.evolutionChain.map((name, index) => (
                  <div className="evolution-item" key={`${name}-${index}`}>
                    <span>{formatPokemonName(name)}</span>
                    {index < pokemon.evolutionChain.length - 1 && <ArrowRight size={18} />}
                  </div>
                )) : <span>{ptBR.detail.noEvolution}</span>}
              </div>
            </section>}
          </>
        )}
      </aside>
    </div>
  );
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

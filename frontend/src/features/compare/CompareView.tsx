import { Swords, Trash2 } from 'lucide-react';
import { StatBar } from '../../shared/components/StatBar';
import { TypeBadge } from '../../shared/components/TypeBadge';
import { useI18n } from '../../shared/i18n/I18nProvider';
import { assetUrl } from '../../shared/utils/assets';
import { formatPokemonName, formatPokemonNumber } from '../../shared/utils/format';
import type { PokemonDetail, PokemonSummary } from '../pokemon/types/pokemon';

type Props = {
  compareSelection: PokemonSummary[];
  compareDetails: PokemonDetail[];
  isLoading: boolean;
  onClear: () => void;
  onOpen: (pokemon: PokemonSummary) => void;
};

const statOrder = ['hp', 'attack', 'defense', 'special-attack', 'special-defense', 'speed'];

export function CompareView({ compareSelection, compareDetails, isLoading, onClear, onOpen }: Props) {
  const { messages } = useI18n();

  if (compareSelection.length < 2) {
    return (
      <section className="empty-state empty-state--compare">
        <Swords size={42} />
        <h2>{messages.compare.title}</h2>
        <p>{messages.compare.emptyDescription}</p>
      </section>
    );
  }

  if (isLoading || compareDetails.length < 2) {
    return <section className="empty-state"><div className="pokeball-loader" /><h2>{messages.compare.loading}</h2></section>;
  }

  const [first, second] = compareDetails;

  return (
    <section className="compare-board">
      <header className="compare-header">
        <div>
          <span className="eyebrow-line"><Swords size={17} /> {messages.compare.eyebrow}</span>
          <h2>{formatPokemonName(first.name)} vs {formatPokemonName(second.name)}</h2>
          <p>{messages.compare.description}</p>
        </div>
        <button className="secondary-control" onClick={onClear}><Trash2 size={17} /> {messages.common.clear}</button>
      </header>

      <div className="fighters-grid">
        {[first, second].map((pokemon) => (
          <button className="fighter-card" key={pokemon.id} onClick={() => onOpen(pokemon)}>
            <span>{formatPokemonNumber(pokemon.id)}</span>
            <img src={assetUrl(pokemon.imageUrl)} alt={pokemon.name} />
            <strong>{formatPokemonName(pokemon.name)}</strong>
            <div className="type-list">
              {pokemon.types.map((type) => <TypeBadge type={type} compact key={type} />)}
            </div>
          </button>
        ))}
      </div>

      <div className="compare-table">
        {statOrder.map((statName) => {
          const firstStat = first.stats.find((stat) => stat.name === statName)?.value ?? 0;
          const secondStat = second.stats.find((stat) => stat.name === statName)?.value ?? 0;
          return (
            <div className="compare-row" key={statName}>
              <div className={firstStat >= secondStat ? 'stat-winner' : ''}>{firstStat}</div>
              <strong>{statLabel(statName, messages)}</strong>
              <div className={secondStat >= firstStat ? 'stat-winner' : ''}>{secondStat}</div>
            </div>
          );
        })}
      </div>

      <div className="detail-two-columns">
        <section className="detail-card-panel">
          <h3>{formatPokemonName(first.name)}</h3>
          {first.stats.map((stat) => <StatBar key={stat.name} name={stat.name} value={stat.value} />)}
        </section>
        <section className="detail-card-panel">
          <h3>{formatPokemonName(second.name)}</h3>
          {second.stats.map((stat) => <StatBar key={stat.name} name={stat.name} value={stat.value} />)}
        </section>
      </div>
    </section>
  );
}

function statLabel(name: string, messages: ReturnType<typeof useI18n>['messages']) {
  const map: Record<string, string> = {
    hp: messages.attributes.hp,
    attack: messages.attributes.attack,
    defense: messages.attributes.defense,
    'special-attack': messages.attributes.specialAttack,
    'special-defense': messages.attributes.specialDefense,
    speed: messages.attributes.speed
  };

  return map[name] ?? name;
}

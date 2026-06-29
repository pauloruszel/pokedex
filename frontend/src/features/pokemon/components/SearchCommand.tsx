import { Filter, Search, SlidersHorizontal, X } from 'lucide-react';
import { TypeBadge } from '../../../shared/components/TypeBadge';

type Props = {
  search: string;
  selectedType: string;
  types: string[];
  onSearchChange: (value: string) => void;
  onSubmit: () => void;
  onTypeChange: (type: string) => void;
  onClear: () => void;
};

const quickTypes = ['fire', 'water', 'grass', 'electric', 'dragon', 'ghost'];

export function SearchCommand({ search, selectedType, types, onSearchChange, onSubmit, onTypeChange, onClear }: Props) {
  return (
    <section className="command-center">
      <div className="search-command">
        <Search size={21} />
        <input
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') onSubmit();
          }}
          placeholder="Busque por nome ou número: pikachu, charizard, 25..."
        />
        {search && <button className="ghost-control" onClick={onClear}><X size={18} /></button>}
        <button className="primary-control" onClick={onSubmit}>Buscar</button>
      </div>

      <div className="filter-strip">
        <span><Filter size={16} /> Tipos rápidos</span>
        {quickTypes.map((type) => (
          <button className={selectedType === type ? 'chip-button chip-button--active' : 'chip-button'} key={type} onClick={() => onTypeChange(type)}>
            <TypeBadge type={type} compact />
          </button>
        ))}
        <label className="select-shell">
          <SlidersHorizontal size={16} />
          <select value={selectedType} onChange={(event) => onTypeChange(event.target.value)}>
            <option value="all">Todos</option>
            {types.map((type) => <option value={type} key={type}>{type}</option>)}
          </select>
        </label>
      </div>
    </section>
  );
}

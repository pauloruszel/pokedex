import { Filter, Search, SlidersHorizontal, X } from 'lucide-react';
import { TypeBadge } from '../../../shared/components/TypeBadge';
import { useI18n } from '../../../shared/i18n/I18nProvider';

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
  const { messages } = useI18n();

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
          placeholder={messages.search.placeholder}
        />
        {search && <button className="ghost-control" onClick={onClear}><X size={18} /></button>}
        <button className="primary-control" onClick={onSubmit}>{messages.search.submit}</button>
      </div>

      <div className="filter-strip">
        <span><Filter size={16} /> {messages.search.quickTypes}</span>
        {quickTypes.map((type) => (
          <button className={selectedType === type ? 'chip-button chip-button--active' : 'chip-button'} key={type} onClick={() => onTypeChange(type)}>
            <TypeBadge type={type} compact />
          </button>
        ))}
        <label className="select-shell">
          <SlidersHorizontal size={16} />
          <select value={selectedType} onChange={(event) => onTypeChange(event.target.value)}>
            <option value="all">{messages.search.allTypes}</option>
            {types.map((type) => (
              <option value={type} key={type}>
                {messages.types[type as keyof typeof messages.types] ?? type}
              </option>
            ))}
          </select>
        </label>
      </div>
    </section>
  );
}

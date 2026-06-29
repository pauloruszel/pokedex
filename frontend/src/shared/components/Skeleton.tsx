export function PokemonGridSkeleton() {
  return (
    <div className="pokemon-grid">
      {Array.from({ length: 12 }).map((_, index) => (
        <div className="skeleton-card" key={index}>
          <div />
          <span />
          <strong />
        </div>
      ))}
    </div>
  );
}

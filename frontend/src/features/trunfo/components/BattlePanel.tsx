import { ArrowRight, Bot, RotateCcw, Trophy, UserRound } from 'lucide-react';
import { formatPokemonName } from '../../../shared/utils/format';
import type { RoundHistoryItem, TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfo';
import { ATTRIBUTE_OPTIONS, formatAttributeValue, getAttributeLabel } from '../utils/trunfoRules';
import { TrunfoCard } from './TrunfoCard';

type Props = {
  status: string;
  round: number;
  playerDeckCount: number;
  cpuDeckCount: number;
  playerCard: TrunfoCardModel | null;
  cpuCard: TrunfoCardModel | null;
  selectedAttribute: TrunfoAttributeKey | null;
  roundResult: RoundHistoryItem | null;
  winner: string | null;
  cpuSuggestion: TrunfoAttributeKey | null;
  onPlay: (attribute: TrunfoAttributeKey) => void;
  onNext: () => void;
  onReset: () => void;
};

export function BattlePanel({
  status,
  round,
  playerDeckCount,
  cpuDeckCount,
  playerCard,
  cpuCard,
  selectedAttribute,
  roundResult,
  winner,
  cpuSuggestion,
  onPlay,
  onNext,
  onReset
}: Props) {
  const isRevealed = status === 'revealed' || status === 'finished';
  const canPlay = status === 'ready';

  return (
    <section className="trunfo-battle">
      <header className="trunfo-scoreboard">
        <div>
          <span><UserRound size={16} /> Você</span>
          <strong>{playerDeckCount}</strong>
        </div>
        <div className="trunfo-round-pill">Rodada {round}</div>
        <div>
          <span><Bot size={16} /> CPU</span>
          <strong>{cpuDeckCount}</strong>
        </div>
      </header>

      <div className="trunfo-table">
        <TrunfoCard
          card={playerCard}
          side="player"
          selectedAttribute={selectedAttribute}
          winningAttribute={roundResult?.result === 'player' ? selectedAttribute : null}
        />

        <div className="trunfo-vs-panel">
          {winner ? (
            <>
              <Trophy size={34} />
              <strong>{winner} venceu a partida</strong>
              <button className="primary-control" onClick={onReset}><RotateCcw size={17} /> Nova partida</button>
            </>
          ) : roundResult ? (
            <>
              <span className={`trunfo-result trunfo-result--${roundResult.result}`}>
                {roundResult.result === 'player' ? 'Você venceu' : roundResult.result === 'cpu' ? 'CPU venceu' : 'Empate'}
              </span>
              <strong>{getAttributeLabel(roundResult.attribute)}</strong>
              <p>
                {formatPokemonName(roundResult.playerName)} {formatAttributeValue(roundResult.attribute, roundResult.playerValue)}
                {' '}x{' '}
                {formatAttributeValue(roundResult.attribute, roundResult.cpuValue)} {formatPokemonName(roundResult.cpuName)}
              </p>
              <button className="primary-control" onClick={onNext}>Próxima rodada <ArrowRight size={17} /></button>
            </>
          ) : (
            <>
              <span className="trunfo-result">Escolha o atributo</span>
              <p>A CPU está com a carta fechada. O atributo escolhido define quem leva as duas cartas.</p>
              {cpuSuggestion && <small>CPU jogaria forte em {getAttributeLabel(cpuSuggestion)}.</small>}
            </>
          )}
        </div>

        <TrunfoCard
          card={cpuCard}
          side="cpu"
          isHidden={!isRevealed}
          selectedAttribute={selectedAttribute}
          winningAttribute={roundResult?.result === 'cpu' ? selectedAttribute : null}
        />
      </div>

      <div className="trunfo-attribute-picker">
        {ATTRIBUTE_OPTIONS.map((option) => (
          <button
            className={selectedAttribute === option.key ? 'trunfo-attribute-button trunfo-attribute-button--active' : 'trunfo-attribute-button'}
            disabled={!canPlay}
            key={option.key}
            onClick={() => onPlay(option.key)}
          >
            <span>{option.shortLabel}</span>
            <strong>{playerCard ? formatAttributeValue(option.key, playerCard.attributes[option.key]) : '-'}</strong>
          </button>
        ))}
      </div>
    </section>
  );
}

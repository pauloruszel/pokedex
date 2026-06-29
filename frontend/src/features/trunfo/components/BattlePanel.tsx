import { ArrowRight, Bot, RotateCcw, Trophy, UserRound } from 'lucide-react';
import { useI18n } from '../../../shared/i18n/I18nProvider';
import { formatPokemonName } from '../../../shared/utils/format';
import type { RoundHistoryItem, TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfo';
import { ATTRIBUTE_OPTIONS, formatAttributeValue, getAttributeLabel, getAttributeShortLabel } from '../utils/trunfoRules';
import { TrunfoCard } from './TrunfoCard';

type Props = {
  status: string;
  round: number;
  playerDeckCount: number;
  cpuDeckCount: number;
  disputePileCount: number;
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
  disputePileCount,
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
  const { messages, format } = useI18n();
  const isRevealed = status === 'revealed' || status === 'finished';
  const canPlay = status === 'ready';
  const winnerLabel = winner === 'Você' ? messages.common.you : winner === 'Empate' ? messages.common.draw : winner;

  return (
    <section className="trunfo-battle">
      <header className="trunfo-scoreboard">
        <div>
          <span><UserRound size={16} /> {messages.common.you}</span>
          <strong>{playerDeckCount}</strong>
        </div>
        <div className="trunfo-round-pill">{messages.trunfo.round} {round}</div>
        <div>
          <span><Bot size={16} /> CPU</span>
          <strong>{cpuDeckCount}</strong>
        </div>
      </header>

      {disputePileCount > 0 && (
        <div className="trunfo-dispute-banner">
          {format(messages.trunfo.dispute, { count: disputePileCount + 2 })}
        </div>
      )}

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
              <strong>{format(messages.trunfo.matchWinner, { winner: winnerLabel ?? '' })}</strong>
              <button className="primary-control" onClick={onReset}><RotateCcw size={17} /> {messages.trunfo.newMatch}</button>
            </>
          ) : roundResult ? (
            <>
              <span className={`trunfo-result trunfo-result--${roundResult.result}`}>
                {roundResult.result === 'player' ? messages.trunfo.playerWon : roundResult.result === 'cpu' ? messages.trunfo.cpuWon : messages.common.draw}
              </span>
              <strong>{getAttributeLabel(roundResult.attribute, messages)}</strong>
              <p>
                {formatPokemonName(roundResult.playerName)} {formatAttributeValue(roundResult.attribute, roundResult.playerValue)}
                {' '}x{' '}
                {formatAttributeValue(roundResult.attribute, roundResult.cpuValue)} {formatPokemonName(roundResult.cpuName)}
              </p>
              {roundResult.result === 'draw' && (
                <small>{format(messages.trunfo.potDraw, { count: roundResult.potSize })}</small>
              )}
              <button className="primary-control" onClick={onNext}>{messages.trunfo.nextRound} <ArrowRight size={17} /></button>
            </>
          ) : (
            <>
              <span className="trunfo-result">{messages.trunfo.chooseAttribute}</span>
              <p>{messages.trunfo.cpuHidden}</p>
              {cpuSuggestion && <small>{format(messages.trunfo.cpuSuggestion, { attribute: getAttributeLabel(cpuSuggestion, messages) })}</small>}
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
            <span>{getAttributeShortLabel(option.key, messages)}</span>
            <strong>{playerCard ? formatAttributeValue(option.key, playerCard.attributes[option.key]) : '-'}</strong>
          </button>
        ))}
      </div>
    </section>
  );
}

import { ArrowRight, Bot, RotateCcw, Trophy, UserRound, Users } from 'lucide-react';
import { useI18nFormat, useMessages } from '../../../shared/i18n/I18nProvider';
import { formatPokemonName } from '../../../shared/utils/format';
import type { TrunfoAttributeKey, TrunfoCardModel } from '../types/trunfoCard';
import type { PlayerSide, RoundHistoryItem, TrunfoGameMode } from '../types/trunfoGame';
import { ATTRIBUTE_OPTIONS, formatAttributeValue, getAttributeLabel, getAttributeShortLabel } from '../utils/trunfoAttributes';
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
  gameMode: TrunfoGameMode;
  currentTurn: PlayerSide;
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
  gameMode,
  currentTurn,
  onPlay,
  onNext,
  onReset
}: Props) {
  const messages = useMessages();
  const format = useI18nFormat();
  const isLocalPvp = gameMode === 'local-pvp';
  const isRevealed = status === 'revealed' || status === 'finished';
  const canPlay = status === 'ready';
  const activeCard = currentTurn === 'player-one' ? playerCard : cpuCard;
  const winnerLabel = winner === 'Você' ? messages.common.you : winner === 'Empate' ? messages.common.draw : winner;
  const playerOneLabel = isLocalPvp ? 'Jogador 1' : messages.common.you;
  const playerTwoLabel = isLocalPvp ? 'Jogador 2' : 'CPU';

  return (
    <section className="trunfo-battle">
      <header className="trunfo-scoreboard">
        <div>
          <span><UserRound size={16} /> {playerOneLabel}</span>
          <strong>{playerDeckCount}</strong>
        </div>
        <div className="trunfo-round-pill">
          {messages.trunfo.round} {round}
          {isLocalPvp && status === 'ready' && <small> · vez do {currentTurn === 'player-one' ? 'Jogador 1' : 'Jogador 2'}</small>}
        </div>
        <div>
          <span>{isLocalPvp ? <Users size={16} /> : <Bot size={16} />} {playerTwoLabel}</span>
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
          isHidden={isLocalPvp && currentTurn === 'player-two' && !isRevealed}
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
                {roundResult.result === 'player'
                  ? (isLocalPvp ? 'Jogador 1 venceu a rodada' : messages.trunfo.playerWon)
                  : roundResult.result === 'cpu'
                    ? (isLocalPvp ? 'Jogador 2 venceu a rodada' : messages.trunfo.cpuWon)
                    : messages.common.draw}
              </span>
              <strong>{getAttributeLabel(roundResult.attribute, messages)}</strong>
              <p>
                {formatPokemonName(roundResult.playerName)} {formatAttributeValue(roundResult.attribute, roundResult.playerValue)}
                {' '}x{' '}
                {formatAttributeValue(roundResult.attribute, roundResult.cpuValue)} {formatPokemonName(roundResult.cpuName)}
              </p>
              {roundResult.result === 'draw' && <small>{format(messages.trunfo.potDraw, { count: roundResult.potSize })}</small>}
              <button className="primary-control" onClick={onNext}>{messages.trunfo.nextRound} <ArrowRight size={17} /></button>
            </>
          ) : (
            <>
              <span className="trunfo-result">{messages.trunfo.chooseAttribute}</span>
              <p>{isLocalPvp ? 'O adversário deve olhar para o outro lado enquanto o jogador da vez escolhe.' : messages.trunfo.cpuHidden}</p>
              {cpuSuggestion && <small>{format(messages.trunfo.cpuSuggestion, { attribute: getAttributeLabel(cpuSuggestion, messages) })}</small>}
            </>
          )}
        </div>

        <TrunfoCard
          card={cpuCard}
          side="cpu"
          isHidden={!isRevealed && (!isLocalPvp || currentTurn === 'player-one')}
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
            <strong>{activeCard ? formatAttributeValue(option.key, activeCard.attributes[option.key]) : '-'}</strong>
          </button>
        ))}
      </div>
    </section>
  );
}

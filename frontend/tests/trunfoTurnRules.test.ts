import assert from 'node:assert/strict';
import test from 'node:test';
import { applyRoundResult, localWinnerLabel, nextTurnForResult } from '../src/features/trunfo/utils/trunfoTurnRules.ts';

test('vencedor da rodada assume o próximo turno', () => {
  assert.equal(nextTurnForResult('player', 'player-two'), 'player-one');
  assert.equal(nextTurnForResult('cpu', 'player-one'), 'player-two');
});

test('empate mantém o jogador que já tinha a vez', () => {
  assert.equal(nextTurnForResult('draw', 'player-one'), 'player-one');
  assert.equal(nextTurnForResult('draw', 'player-two'), 'player-two');
});

test('identifica corretamente o vencedor da partida local', () => {
  assert.equal(localWinnerLabel(3, 0), 'Jogador 1');
  assert.equal(localWinnerLabel(0, 4), 'Jogador 2');
  assert.equal(localWinnerLabel(0, 0), 'Empate');
  assert.equal(localWinnerLabel(2, 2), null);
});

test('vencedor recebe cartas da rodada e monte acumulado', () => {
  const next = applyRoundResult('player', ['p1', 'p2'], ['c1', 'c2'], ['pot1', 'pot2']);

  assert.deepEqual(next.playerOneDeck, ['p2', 'pot1', 'pot2', 'p1', 'c1']);
  assert.deepEqual(next.playerTwoDeck, ['c2']);
  assert.deepEqual(next.disputePile, []);
});

test('empate remove cartas da mesa e acumula o monte', () => {
  const next = applyRoundResult('draw', ['p1', 'p2'], ['c1', 'c2'], ['pot1']);

  assert.deepEqual(next.playerOneDeck, ['p2']);
  assert.deepEqual(next.playerTwoDeck, ['c2']);
  assert.deepEqual(next.disputePile, ['pot1', 'p1', 'c1']);
});

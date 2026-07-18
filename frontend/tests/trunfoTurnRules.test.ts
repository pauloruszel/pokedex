import assert from 'node:assert/strict';
import test from 'node:test';
import { localWinnerLabel, nextTurnForResult } from '../src/features/trunfo/utils/trunfoTurnRules.ts';

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

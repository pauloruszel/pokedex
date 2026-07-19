import assert from 'node:assert/strict';
import test from 'node:test';
import { readApiErrorMessage } from '../src/features/trunfo/api/apiError.ts';

test('readApiErrorMessage usa mensagem do backend', async () => {
  const response = {
    json: async () => ({ message: 'Carta ja escolhida pelo adversario.' })
  } as Response;

  assert.equal(await readApiErrorMessage(response, 'fallback'), 'Carta ja escolhida pelo adversario.');
});

import assert from 'node:assert/strict';
import {test} from 'node:test';
import {formatSafeError, OperationTimeoutError} from '../../../scripts/performance-tests/nft-shared.mjs';

test('formats timeout and cleanup AggregateError details without exposing unexpected errors', () => {
  const awsError = new Error('AWS ecs describe-services failed (status 255, AccessDenied)');
  awsError.safeToLog = true;
  const error = new AggregateError([
    new OperationTimeoutError('App rollout timeout'),
    awsError,
    new Error('private environment value'),
  ], 'cleanup contained private environment value');

  assert.equal(formatSafeError(error),
    'Multiple operations failed: App rollout timeout; AWS ecs describe-services failed (status 255, AccessDenied); Error details redacted');
});

test('formats known recovery validation messages and strips control characters', () => {
  const error = new Error('Pinned object unavailable\nprivate context');
  error.name = 'SafeRecoveryError';

  assert.equal(formatSafeError(error), 'Pinned object unavailable private context');
});

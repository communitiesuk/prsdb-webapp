import assert from 'node:assert/strict';
import {test} from 'node:test';
import {readFile} from 'node:fs/promises';
import {acquireLock, releaseLock, resizeDatabase} from '../../../scripts/performance-tests/nft-rds-resize.mjs';

function fixture() {
  const calls = [];
  const state = {runId: '123-1', writersStopped: true};
  let instance = {
    DBInstanceIdentifier: 'nft-database', DBInstanceStatus: 'available',
    DBInstanceClass: 'db.t4g.small', PendingModifiedValues: {},
  };
  const deps = {
    state, calls,
    persist: async () => calls.push(['persist', structuredClone(state)]),
    checkpoint: async () => calls.push(['checkpoint', structuredClone(state)]),
    sleep: async () => {}, now: () => 0, timeoutMs: 1, pollMs: 1,
    aws: async (service, operation, input) => {
      calls.push([service, operation, structuredClone(input)]);
      if (operation === 'get-caller-identity') return {Account: '448120078528'};
      if (operation === 'get-item') return {Item: {Owner: {S: 'performance-123-1'}}};
      if (operation === 'describe-db-instances') return {DBInstances: [structuredClone(instance)]};
      if (operation === 'modify-db-instance') instance = {...instance, DBInstanceClass: input.DBInstanceClass};
      return {};
    },
    setInstance: value => { instance = {...instance, ...value}; },
  };
  return deps;
}

test('lock acquisition and release use conditional ownership', async () => {
  const deps = fixture();
  await acquireLock('123-1', deps);
  await releaseLock('123-1', deps);
  const acquire = deps.calls.find(call => call[1] === 'put-item')[2];
  const release = deps.calls.find(call => call[1] === 'delete-item')[2];
  assert.equal(acquire.ConditionExpression, 'attribute_not_exists(LockID)');
  assert.equal(acquire.Item.Owner.S, 'performance-123-1');
  assert.equal(acquire.ReturnConsumedCapacity, 'TOTAL');
  assert.equal(release.ConditionExpression, '#owner = :owner');
  assert.equal(release.ExpressionAttributeValues[':owner'].S, 'performance-123-1');
  assert.equal(release.ReturnConsumedCapacity, 'TOTAL');
});

test('resize checkpoints intent before modification and returns to the original class', async () => {
  const deps = fixture();
  await resizeDatabase('123-1', 'up', deps);
  assert.equal(deps.state.database.originalClass, 'db.t4g.small');
  assert.equal(deps.state.database.upComplete, true);
  assert.ok(deps.calls.findIndex(call => call[0] === 'checkpoint') <
    deps.calls.findIndex(call => call[1] === 'modify-db-instance'));
  await resizeDatabase('123-1', 'down', deps);
  assert.equal(deps.state.database.downComplete, true);
  assert.deepEqual(deps.calls.filter(call => call[1] === 'modify-db-instance').map(call => call[2].DBInstanceClass),
    ['db.m8g.2xlarge', 'db.t4g.small']);
});

test('refuses to resize when another modification is pending', async () => {
  const deps = fixture();
  deps.setInstance({PendingModifiedValues: {StorageType: 'gp2'}});
  await assert.rejects(resizeDatabase('123-1', 'up', deps), /pending/i);
  assert.equal(deps.calls.some(call => call[1] === 'modify-db-instance'), false);
});

test('refuses to modify an unexpected baseline or run without stopped writers', async () => {
  const deps = fixture();
  deps.setInstance({DBInstanceClass: 'db.r7g.large'});
  await assert.rejects(resizeDatabase('123-1', 'up', deps), /baseline/i);
  deps.state.writersStopped = false;
  await assert.rejects(resizeDatabase('123-1', 'up', deps), /writers/i);
});

test('refuses recovery without ownership or with a conflicting class', async () => {
  const deps = fixture();
  await resizeDatabase('123-1', 'up', deps);
  deps.setInstance({DBInstanceClass: 'db.r7g.large'});
  await assert.rejects(resizeDatabase('123-1', 'down', deps), /unexpected/i);
  deps.aws = async (service, operation) => operation === 'get-caller-identity'
    ? {Account: '448120078528'} : {Item: {Owner: {S: 'performance-999-1'}}};
  await assert.rejects(resizeDatabase('123-1', 'down', deps), /lock/i);
});

test('recognizes a completed resize when the modify response was lost', async () => {
  const deps = fixture();
  const originalAws = deps.aws;
  deps.aws = async (...args) => {
    const result = await originalAws(...args);
    if (args[1] === 'modify-db-instance') throw new Error('response lost');
    return result;
  };
  await assert.rejects(resizeDatabase('123-1', 'up', deps), /response lost/);
  deps.aws = originalAws;
  await resizeDatabase('123-1', 'up', deps);
  assert.equal(deps.state.database.upComplete, true);
  assert.equal(deps.calls.filter(call => call[1] === 'modify-db-instance').length, 1);
});

test('times out without claiming resize completion', async () => {
  const deps = fixture();
  const originalAws = deps.aws;
  let time = 0;
  deps.now = () => time++;
  deps.aws = async (...args) => {
    if (args[1] === 'modify-db-instance') {
      deps.calls.push(['rds', 'modify-db-instance', args[2]]);
      deps.setInstance({DBInstanceStatus: 'modifying', PendingModifiedValues: {DBInstanceClass: 'db.m8g.2xlarge'}});
      return {};
    }
    return originalAws(...args);
  };
  await assert.rejects(resizeDatabase('123-1', 'up', deps), /timeout/i);
  assert.equal(deps.state.database.upAttempted, true);
  assert.notEqual(deps.state.database.upComplete, true);
});

test('does not submit a resize after cancellation', async () => {
  const deps = fixture();
  deps.signal = AbortSignal.abort(new Error('cancelled'));
  await assert.rejects(resizeDatabase('123-1', 'up', deps), /cancelled/);
  assert.equal(deps.state.database.upAttempted, true);
  assert.equal(deps.calls.some(call => call[1] === 'modify-db-instance'), false);
});

test('workflow holds the lock from before NFT mutations until verified recovery', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const acquire = workflow.indexOf('nft-rds-resize.mjs acquire');
  const pause = workflow.indexOf('nft-vpc-restore.mjs pause-maintenance');
  const up = workflow.indexOf('nft-vpc-restore.mjs resize-up');
  const seed = workflow.indexOf('nft-vpc-restore.mjs run');
  const down = workflow.indexOf('nft-vpc-restore.mjs resize-down');
  const start = workflow.indexOf('nft-vpc-restore.mjs start-after-reset');
  const schedules = workflow.indexOf('nft-vpc-restore.mjs restore-maintenance');
  const release = workflow.indexOf('nft-rds-resize.mjs release');
  assert.ok(acquire >= 0 && acquire < pause && pause < up && up < seed);
  assert.ok(seed < down && down < start && start < schedules && schedules < release);
  const asyncJob = workflow.slice(workflow.indexOf('  restore-async-after-database-reset:'),
    workflow.indexOf('  restore-maintenance-schedules:'));
  assert.match(asyncJob, /needs\.resize-database-after-reset\.result == 'success'/);
  assert.match(workflow, /nft-lock-recovery-required:/);
});

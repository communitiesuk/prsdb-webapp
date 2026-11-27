import {pathToFileURL} from 'node:url';
import {readFile} from 'node:fs/promises';
import {join} from 'node:path';
import {account, awsCommand, requireCondition, waitFactory} from './nft-shared.mjs';
import {recoveryOwner} from './nft-recovery-state.mjs';

const table = 'tfstate-lock-nft';
const key = 'nft-performance-operation';
const database = 'nft-database';
const baseline = 'db.t4g.small';
const performance = 'db.m8g.2xlarge';

function owner(runId) {
  requireCondition(/^[1-9]\d*-[1-9]\d*$/.test(runId ?? ''), 'Invalid NFT performance run identity');
  return `performance-${runId}`;
}

function lockKey() {
  return {LockID: {S: key}};
}

export async function acquireLock(runId, {aws = awsCommand} = {}) {
  const identity = owner(runId);
  requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');
  await aws('dynamodb', 'put-item', {
    TableName: table, Item: {...lockKey(), Owner: {S: identity}},
    ConditionExpression: 'attribute_not_exists(LockID)', ReturnConsumedCapacity: 'TOTAL',
  });
}

export async function releaseLock(runId, {aws = awsCommand} = {}) {
  const identity = owner(runId);
  requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');
  await aws('dynamodb', 'delete-item', {
    TableName: table, Key: lockKey(), ConditionExpression: '#owner = :owner',
    ExpressionAttributeNames: {'#owner': 'Owner'}, ExpressionAttributeValues: {':owner': {S: identity}},
    ReturnConsumedCapacity: 'TOTAL',
  });
}

async function assertLock(runId, aws) {
  const result = await aws('dynamodb', 'get-item', {TableName: table, Key: lockKey(), ConsistentRead: true});
  requireCondition(result.Item?.Owner?.S === owner(runId), 'NFT performance lock is not owned by this run');
}

async function describe(aws) {
  const result = await aws('rds', 'describe-db-instances', {DBInstanceIdentifier: database});
  requireCondition(result.DBInstances?.length === 1 &&
    result.DBInstances[0].DBInstanceIdentifier === database, 'Unexpected NFT database identity');
  return result.DBInstances[0];
}

function pendingMatches(instance, target) {
  const pending = instance.PendingModifiedValues ?? {};
  return Object.keys(pending).every(name => name === 'DBInstanceClass' && pending[name] === target);
}

export async function resizeDatabase(runId, direction, overrides = {}) {
  const deps = {
    aws: awsCommand, sleep: ms => new Promise(resolve => setTimeout(resolve, ms)), now: Date.now,
    timeoutMs: 2400000, pollMs: 15000, ...overrides,
  };
  const {aws, state, persist, checkpoint} = deps;
  requireCondition(['up', 'down'].includes(direction), 'Invalid NFT database resize direction');
  requireCondition(state?.runId === runId && state.writersStopped === true,
    'Stopped NFT writers and matching recovery state are required');
  requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');
  await assertLock(runId, aws);
  let instance = await describe(aws);
  const target = direction === 'up' ? performance : state.database?.originalClass;
  requireCondition(direction === 'up' || target === baseline, 'NFT database baseline recovery state is missing');
  const progress = state.database ?? {};
  const attempted = direction === 'up' ? 'upAttempted' : 'downAttempted';
  const complete = direction === 'up' ? 'upComplete' : 'downComplete';

  if (!progress[attempted]) {
    requireCondition(instance.DBInstanceStatus === 'available' &&
      Object.keys(instance.PendingModifiedValues ?? {}).length === 0, 'NFT database has pending modifications or is unavailable');
    requireCondition(instance.DBInstanceClass === (direction === 'up' ? baseline : performance),
      direction === 'up' ? 'Unexpected NFT database baseline' : 'Unexpected NFT database class during recovery');
    if (direction === 'up') progress.originalClass = instance.DBInstanceClass;
    progress[attempted] = true;
    state.database = progress;
    await persist();
    await checkpoint();
    deps.signal?.throwIfAborted();
    await aws('rds', 'modify-db-instance', {
      DBInstanceIdentifier: database, DBInstanceClass: target, ApplyImmediately: true,
    });
  } else {
    requireCondition(progress.originalClass === baseline, 'Unexpected NFT database baseline recovery state');
    requireCondition(instance.DBInstanceClass === target ||
      instance.PendingModifiedValues?.DBInstanceClass === target,
    'Unexpected NFT database state after resize request; operator recovery required');
  }

  await waitFactory(deps)('NFT database resize', async () => {
    instance = await describe(aws);
    requireCondition(pendingMatches(instance, target), 'NFT database has conflicting pending modifications');
    requireCondition([baseline, performance].includes(instance.DBInstanceClass),
      'Unexpected NFT database class during resize');
    return instance.DBInstanceStatus === 'available' && instance.DBInstanceClass === target &&
      Object.keys(instance.PendingModifiedValues ?? {}).length === 0;
  }, false, deps.signal);
  progress[complete] = true;
  await persist();
  await checkpoint();
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    if (process.argv[2] === 'acquire') {
      await acquireLock(`${process.env.GITHUB_RUN_ID}-${process.env.GITHUB_RUN_ATTEMPT}`);
    } else if (process.argv[2] === 'release') {
      const path = process.env.NFT_RESTORE_STATE_PATH ?? join(process.env.RUNNER_TEMP, 'nft-restore-state.json');
      const state = JSON.parse(await readFile(path, 'utf8'));
      const runId = recoveryOwner(state, {runId: process.env.GITHUB_RUN_ID, attempt: process.env.GITHUB_RUN_ATTEMPT});
      requireCondition(state.database?.downComplete === true && state.maintenanceRestored === true &&
        state.qaVerified === true && state.asyncRestored === true, 'NFT recovery is incomplete; retaining lock');
      await releaseLock(runId);
    }
    else throw new Error('Unknown NFT performance lock operation');
  } catch (error) {
    console.error(`NFT performance lock operation failed: ${error.message}`);
    process.exitCode = 1;
  }
}

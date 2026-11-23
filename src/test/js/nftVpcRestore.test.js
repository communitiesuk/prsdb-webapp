import assert from 'node:assert/strict';
import {mkdtemp, mkdir, readFile, rm, writeFile} from 'node:fs/promises';
import {spawnSync} from 'node:child_process';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {fileURLToPath} from 'node:url';
import {test} from 'node:test';
import {cleanupNftEnvironment, resolveRestoreTaskDefinition, restoreAsyncWorker, restoreInVpc, startNftApp, stopNftWriters, stopRestoreTask} from '../../../scripts/performance-tests/nft-vpc-restore.mjs';
import {restoreEnvironment} from '../../../scripts/performance-tests/nft-restore.mjs';
import * as lifecycle from '../../../scripts/performance-tests/nft-vpc-restore.mjs';

const prefix = 'arn:aws:ecs:eu-west-2:448120078528:';
const appArn = `${prefix}task-definition/prsdb-webapp-nft:95`;
const restoreFamily = 'prsdb-nft-vpc-restore-performance';
const workerArn = `${prefix}task-definition/${restoreFamily}:3`;
const certificateKey = 'seeding/certificate-references/123-1.png';
const certificateVersion = 'certificate-v1';
const asyncArn = `${prefix}task-definition/prsdb-async-worker-nft:4`;
const taskArn = `${prefix}task/nft-app/worker`;
const imageDigest = `sha256:${'c'.repeat(64)}`;
const config = {taskDefinition: workerArn, runId: '123-1', resetSha: 'b'.repeat(40),
  resetCompatibleTaskDefinition: asyncArn, resetCompatibleSha: 'b'.repeat(40)};
const scheduleGroup = 'nft-scheduled-tasks-group';
const scheduledClusterArn = `${prefix.replace(':ecs:', ':ecs:')}cluster/nft-scheduled-tasks`;
const scheduledRoleArn = 'arn:aws:iam::448120078528:role/nft-scheduled-tasks';
const maintenanceScheduleDefinitions = [
  ['nft-jl-invitation-expiry-email-scheduled-task', 'prsdb-jl-invitation-expiry-email-scheduled-task-nft'],
  ['nft-reconcile-payments-scheduled-task', 'prsdb-reconcile-payments-scheduled-task-nft'],
  ['nft-incomplete-property-reminder-scheduled-task', 'prsdb-incomplete-property-reminder-scheduled-task-nft'],
  ['nft-delete-incomplete-properties-scheduled-task', 'prsdb-delete-incomplete-properties-scheduled-task-nft'],
  ['nft-org-ll-invitation-deletion-scheduled-task', 'prsdb-org-ll-invitation-deletion-scheduled-task-nft'],
  ['nft-jl-invitation-deletion-scheduled-task', 'prsdb-jl-invitation-deletion-scheduled-task-nft'],
  ['nft-ngd-address-update-scheduled-task', 'prsdb-ngd-address-update-scheduled-task-nft'],
];

function maintenanceSchedule(name, taskDefinition, state = 'ENABLED') {
  return {
    ActionAfterCompletion: 'NONE',
    Arn: `arn:aws:scheduler:eu-west-2:448120078528:schedule/${scheduleGroup}/${name}`,
    FlexibleTimeWindow: {Mode: 'OFF'},
    GroupName: scheduleGroup,
    Name: name,
    ScheduleExpression: 'cron(0 1 * * ? *)',
    ScheduleExpressionTimezone: 'UTC',
    State: state,
    Target: {
      Arn: scheduledClusterArn,
      EcsParameters: {
        LaunchType: 'FARGATE', TaskCount: 1, TaskDefinitionArn: `arn:aws:ecs:eu-west-2:448120078528:task-definition/${taskDefinition}`,
        NetworkConfiguration: {awsvpcConfiguration: {AssignPublicIp: 'DISABLED', SecurityGroups: ['sg-nft'], Subnets: ['subnet-nft']}},
      },
      RoleArn: scheduledRoleArn,
    },
  };
}
function managedRestoreDefinition(revision, overrides = {}) {
  const taskDefinitionArn = `${prefix}task-definition/${restoreFamily}:${revision}`;
  const environment = {
    NFT_DUMP_VERSION: 'seed-v1',
    NFT_DUMP_BYTES: '123456',
    NFT_DUMP_SHA256: 'a'.repeat(64),
    NFT_CERTIFICATE_KEY: certificateKey,
    NFT_CERTIFICATE_VERSION: certificateVersion,
    NFT_CERTIFICATE_DUMP_VERSION: 'seed-v1',
    NFT_CERTIFICATE_DUMP_BYTES: '123456',
    NFT_CERTIFICATE_DUMP_SHA256: 'a'.repeat(64),
    ...overrides,
  };
  return {
    taskDefinitionArn, family: restoreFamily, status: 'ACTIVE',
    containerDefinitions: [{
      name: 'nft-vpc-restore', essential: true,
      environment: Object.entries(environment).map(([name, value]) => ({name, value})),
    }],
  };
}
const app = () => ({
  serviceName: 'nft-app', status: 'ACTIVE', taskDefinition: appArn,
  desiredCount: 1, runningCount: 1, pendingCount: 0,
  deployments: [{taskDefinition: appArn, rolloutState: 'COMPLETED'}],
  networkConfiguration: {awsvpcConfiguration: {subnets: ['subnet-private'], securityGroups: ['sg-app'], assignPublicIp: 'DISABLED'}},
});
const simulator = {serviceName: 'nft-one-login-simulator', status: 'ACTIVE', desiredCount: 0, runningCount: 0, pendingCount: 0};
const asyncWorker = () => ({
  serviceName: 'nft-async-worker', status: 'ACTIVE', taskDefinition: asyncArn,
  desiredCount: 1, runningCount: 1, pendingCount: 0,
  deployments: [{taskDefinition: asyncArn, rolloutState: 'COMPLETED'}],
});

function dependencies(overrides = {}) {
  const calls = [];
  const state = overrides.state ?? {
    maintenancePaused: true, runId: config.runId, taskDefinition: workerArn,
    restoreDefinition: {taskDefinitionArn: workerArn, dumpVersion: 'seed-v1', dumpBytes: 123456,
      dumpSha256: 'a'.repeat(64), certificateKey, certificateVersion},
  };
  const schedules = new Map(maintenanceScheduleDefinitions.map(([name, taskDefinition]) =>
    [name, maintenanceSchedule(name, taskDefinition)]));
  let stopping = false;
  let asyncDesired = 1;
  let polls = 0;
  return {
    calls, state,
    schedules,
    persist: async () => calls.push(['persist', structuredClone(state)]),
    checkpoint: async () => calls.push(['checkpoint', structuredClone(state)]),
    sleep: async () => {},
    log: () => {},
    aws: async (service, operation, input = {}) => {
      calls.push([operation, structuredClone(input)]);
      if (operation === 'get-caller-identity') return {Account: '448120078528'};
      if (service === 's3api' && operation === 'head-object') return {VersionId: certificateVersion, ContentLength: 42};
      if (service === 'scheduler' && operation === 'list-schedules') {
        return {Schedules: [...schedules.values()].map(({Arn, Name, GroupName, State}) => ({Arn, Name, GroupName, State}))};
      }

      if (service === 'scheduler' && operation === 'get-schedule') {
        return structuredClone(schedules.get(input.Name));
      }
      if (service === 'scheduler' && operation === 'update-schedule') {
        schedules.set(input.Name, {...schedules.get(input.Name), ...structuredClone(input)});
        return {};
      }
      if (operation === 'describe-services') {
        if (input.services.includes('nft-async-worker')) return {services: [{...asyncWorker(), desiredCount: asyncDesired, runningCount: asyncDesired}]};
        return {services: [{...app(), ...(stopping ? {desiredCount: 0, runningCount: 0} : {})}, simulator]};
      }
      if (operation === 'update-service') {
        if (input.service === 'nft-async-worker') asyncDesired = input.desiredCount;
        else stopping = true;
        return {};
      }
      if (operation === 'list-tasks') {
        if (input.cluster === 'nft-scheduled-tasks') return {taskArns: state.scheduledWritersRunning ? ['scheduled-task'] : []};
        if (input.serviceName === 'nft-async-worker') return {taskArns: ['async-task']};
        return {taskArns: input.cluster === 'nft-scheduled-tasks' ? [] : ['app-task']};
      }
      if (operation === 'describe-task-definition') {
        if (input.taskDefinition === asyncArn) return {taskDefinition: {taskDefinitionArn: asyncArn, status: 'ACTIVE'}};
        if (input.taskDefinition === appArn) return {taskDefinition: {
          taskDefinitionArn: appArn, family: 'prsdb-webapp-nft', status: 'ACTIVE',
          containerDefinitions: [{name: 'prsdb-webapp', image: 'nft-webapp:untested'}],
        }};
        return {taskDefinition: managedRestoreDefinition(3)};
      }
      if (operation === 'get-queue-attributes') return {Attributes: {
        QueueArn: `${prefix.replace(':ecs:', ':sqs:')}nft-prsdb-scan-result-queue`,
        ApproximateNumberOfMessages: '0', ApproximateNumberOfMessagesNotVisible: '0', ApproximateNumberOfMessagesDelayed: '0',
      }};
      if (operation === 'list-task-definitions') return {taskDefinitionArns: []};
      if (operation === 'run-task') return {tasks: [{taskArn}]};
      if (operation === 'describe-tasks') {
        if (input.tasks.includes('async-task')) return {tasks: [{taskArn: 'async-task', lastStatus: 'STOPPED'}]};
        if (input.tasks.includes('app-task')) {
          polls++;
          return {tasks: [{taskArn: 'app-task', lastStatus: polls < 2 ? 'DEACTIVATING' : 'STOPPED'}]};
        }
        return {tasks: [{
          taskArn, taskDefinitionArn: workerArn, lastStatus: 'STOPPED',
          containers: [{name: 'nft-vpc-restore', exitCode: 0}],
        }]};
      }
      return {};
    },
    ...overrides,
  };
}

function freshDependencies() {
  return dependencies({state: {maintenancePaused: true}});
}

function deployedCompatibilityDependencies({sha = 'b'.repeat(40), appDigest = imageDigest, workerDigest = imageDigest,
  workerImage} = {}) {
  const deps = dependencies();
  const appImage = `448120078528.dkr.ecr.eu-west-2.amazonaws.com/prsdb-webapp:release-${sha}`;
  const appDefinition = {
    taskDefinitionArn: appArn, family: 'prsdb-webapp-nft', status: 'ACTIVE',
    containerDefinitions: [{name: 'prsdb-webapp', image: appImage}],
  };
  const asyncDefinition = {
    taskDefinitionArn: asyncArn, family: 'prsdb-async-worker-nft', status: 'ACTIVE',
    containerDefinitions: [{name: 'prsdb-async-worker', image: workerImage ?? appImage}],
  };
  const original = deps.aws;
  let appStopped = false;
  let asyncDesired = 1;
  deps.aws = async (service, operation, input = {}) => {
    if (operation === 'describe-task-definition' && input.taskDefinition === appArn) {
      return {taskDefinition: appDefinition};
    }
    if (operation === 'describe-task-definition' && input.taskDefinition === asyncArn) {
      return {taskDefinition: asyncDefinition};
    }
    if (operation === 'describe-tasks' && input.tasks.includes('app-task')) {
      return {tasks: [{taskArn: 'app-task', taskDefinitionArn: appArn,
        lastStatus: appStopped ? 'STOPPED' : 'RUNNING',
        containers: [{name: 'prsdb-webapp', image: appImage, imageDigest: appDigest}]}]};
    }
    if (operation === 'describe-tasks' && input.tasks.includes('async-task')) {
      return {tasks: [{taskArn: 'async-task', taskDefinitionArn: asyncArn,
        lastStatus: asyncDesired === 0 ? 'STOPPED' : 'RUNNING',
        containers: [{name: 'prsdb-async-worker', image: asyncDefinition.containerDefinitions[0].image,
          imageDigest: workerDigest}]}]};
    }
    if (operation === 'update-service') {
      if (input.service === 'nft-app') appStopped = input.desiredCount === 0;
      if (input.service === 'nft-async-worker') asyncDesired = input.desiredCount;
    }
    return original(service, operation, input);
  };
  return deps;
}

function markQaVerified(deps) {
  const appSha = deps.state.expectedSha ?? config.resetSha;
  const resetCompatibility = deps.state.resetCompatibility ?? {
    mode: 'matching-workflow',
    resetSha: config.resetSha,
    appSha,
    appTaskDefinition: deps.state.originalApp?.taskDefinition,
    asyncTaskDefinition: deps.state.originalAsync?.taskDefinition,
    appImageDigest: imageDigest,
    asyncImageDigest: imageDigest,
  };
  Object.assign(deps.state, {appRestored: true, qaVerified: true, expectedSha: appSha, resetCompatibility});
}

test('resolves and checkpoints the newest active managed restore task and immutable dump identity', async () => {
  const deps = freshDependencies();
  const definitions = [managedRestoreDefinition(2), managedRestoreDefinition(11), managedRestoreDefinition(9)];
  deps.aws = async (service, operation, input = {}) => {
    deps.calls.push([operation, structuredClone(input)]);
    if (operation === 'get-caller-identity') return {Account: '448120078528'};
    if (operation === 'list-task-definitions') {
      assert.deepEqual(input, {familyPrefix: restoreFamily, status: 'ACTIVE', sort: 'DESC', maxResults: 100});
      return {taskDefinitionArns: definitions.map(value => value.taskDefinitionArn)};
    }
    if (operation === 'describe-task-definition') {
      return {taskDefinition: definitions.find(value => value.taskDefinitionArn === input.taskDefinition)};
    }
    if (service === 's3api' && operation === 'head-object') return {VersionId: certificateVersion, ContentLength: 42};
    throw new Error(`Unexpected ${service} ${operation}`);
  };

  const resolved = await resolveRestoreTaskDefinition({runId: config.runId}, deps);

  assert.equal(resolved, definitions[1].taskDefinitionArn);
  assert.equal(deps.state.taskDefinition, resolved);
  assert.deepEqual(deps.state.restoreDefinition, {
    taskDefinitionArn: resolved,
    dumpVersion: 'seed-v1',
    dumpBytes: 123456,
    dumpSha256: 'a'.repeat(64),
    certificateKey,
    certificateVersion,
  });
  assert.deepEqual(deps.calls.find(([operation]) => operation === 'head-object')[1], {
    Bucket: 'prsdb-uploaded-files-nft', Key: certificateKey, VersionId: certificateVersion,
  });
  assert.equal(deps.state.runId, config.runId);
});

test('certificate preflight rejects invalid or mismatched bindings before NFT mutations', async t => {
  const cases = [
    {NFT_CERTIFICATE_KEY: undefined}, {NFT_CERTIFICATE_KEY: 'seeding/certificate-references/0-1.png'},
    {NFT_CERTIFICATE_KEY: 'seeding/certificate-references/1-0.png'}, {NFT_CERTIFICATE_KEY: 'seeding/certificate-references/1-1.png/other'},
    {NFT_CERTIFICATE_VERSION: undefined}, {NFT_CERTIFICATE_VERSION: 'null'}, {NFT_CERTIFICATE_VERSION: 'None'}, {NFT_CERTIFICATE_VERSION: 'undefined'},
    {NFT_CERTIFICATE_VERSION: '  '},
    {NFT_CERTIFICATE_DUMP_VERSION: 'other'}, {NFT_CERTIFICATE_DUMP_BYTES: '123457'},
    {NFT_CERTIFICATE_DUMP_SHA256: 'b'.repeat(64)}, {NFT_CERTIFICATE_DUMP_VERSION: undefined},
    {NFT_CERTIFICATE_DUMP_BYTES: undefined}, {NFT_CERTIFICATE_DUMP_SHA256: undefined},
  ];
  for (const metadata of cases) await t.test(JSON.stringify(metadata), async () => {
    const deps = freshDependencies();
    const original = deps.aws;
    deps.aws = async (service, operation, input) => operation === 'describe-task-definition'
      ? {taskDefinition: managedRestoreDefinition(3, metadata)} : original(service, operation, input);
    await assert.rejects(resolveRestoreTaskDefinition(config, deps), /certificate/i);
    assert.equal(deps.state.restoreDefinition, undefined);
    assert.equal(deps.calls.some(([op]) => op === 'head-object'), false);
    assert.equal(deps.calls.some(([op]) => ['update-schedule', 'update-service', 'run-task'].includes(op)), false);
  });
});

test('certificate preflight rejects unavailable or invalid exact object versions safely', async t => {
  for (const result of [new Error('secret AWS access denied'), null, {}, {VersionId: 'other', ContentLength: 4},
    {VersionId: certificateVersion, ContentLength: 0}, {VersionId: certificateVersion, ContentLength: '4'},
    {VersionId: certificateVersion, ContentLength: 4, DeleteMarker: true}]) {
    await t.test(result instanceof Error ? 'access error' : JSON.stringify(result), async () => {
      const deps = freshDependencies();
      const original = deps.aws;
      deps.aws = async (service, operation, input) => {
        if (operation === 'head-object') {
          deps.calls.push([operation, input]);
          if (result instanceof Error) throw result;
          return result;
        }
        return original(service, operation, input);
      };
      await assert.rejects(resolveRestoreTaskDefinition(config, deps), error => {
        assert.match(error.message, /certificate/i);
        assert.doesNotMatch(error.message, /secret AWS/);
        return true;
      });
      assert.equal(deps.state.restoreDefinition, undefined);
      assert.equal(deps.calls.some(([op]) => ['update-schedule', 'update-service', 'run-task'].includes(op)), false);
    });
  }
});

test('certificate preflight accepts the legacy seed certificate key', async () => {
  const deps = freshDependencies();
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'describe-task-definition'
    ? {taskDefinition: managedRestoreDefinition(3, {NFT_CERTIFICATE_KEY: 'seeding/certificate_template.png'})}
    : original(service, operation, input);
  await resolveRestoreTaskDefinition(config, deps);
  assert.equal(deps.state.restoreDefinition.certificateKey, 'seeding/certificate_template.png');
  assert.equal(deps.calls.find(([operation]) => operation === 'head-object')[1].Key, 'seeding/certificate_template.png');
});

test('certificate preflight checkpoint is required before pausing schedules or stopping writers', async () => {
  const deps = freshDependencies();
  await lifecycle.snapshotMaintenanceSchedules(config, deps);
  await assert.rejects(lifecycle.pauseMaintenanceSchedules(config, deps), /certificate.*checkpoint/i);
  deps.state.maintenancePaused = true;
  await assert.rejects(stopNftWriters(config, deps), /certificate.*checkpoint/i);
  assert.equal(deps.calls.some(([op]) => ['update-schedule', 'update-service', 'run-task'].includes(op)), false);
});

test('certificate preflight retry uses the checkpointed revision and rejects changed identity', async () => {
  const deps = freshDependencies();
  await resolveRestoreTaskDefinition(config, deps);
  const saved = structuredClone(deps.state.restoreDefinition);
  await resolveRestoreTaskDefinition({runId: config.runId}, deps);
  assert.equal(deps.calls.some(([op]) => op === 'list-task-definitions'), false);
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'describe-task-definition'
    ? {taskDefinition: managedRestoreDefinition(3, {NFT_CERTIFICATE_VERSION: 'another-version'})}
    : original(service, operation, input);
  await assert.rejects(resolveRestoreTaskDefinition(config, deps), /different immutable dump or certificate identity/);
  assert.deepEqual(deps.state.restoreDefinition, saved);
});

test('certificate preflight precedes schedule changes and is repeated before worker launch', async () => {
  const deps = freshDependencies();
  await resolveRestoreTaskDefinition(config, deps);
  await lifecycle.snapshotMaintenanceSchedules(config, deps);
  await lifecycle.pauseMaintenanceSchedules(config, deps);
  await stopNftWriters(config, deps);
  await restoreInVpc(config, deps);
  const firstHead = deps.calls.findIndex(([op]) => op === 'head-object');
  const firstUpdate = deps.calls.findIndex(([op]) => op === 'update-schedule');
  const lastHead = deps.calls.findLastIndex(([op]) => op === 'head-object');
  const launch = deps.calls.findIndex(([op]) => op === 'run-task');
  assert.ok(firstHead < firstUpdate && firstUpdate < lastHead && lastHead < launch);
  assert.deepEqual(deps.calls[firstHead][1], deps.calls[lastHead][1]);
  assert.equal(deps.calls.filter(([op]) => op === 'head-object').length, 2);
  assert.ok(deps.calls.some(([op, state]) => op === 'checkpoint' &&
    state.restoreDefinition?.certificateVersion === certificateVersion));
});

test('restore task resolution rejects missing or malformed immutable dump metadata', async t => {
  for (const metadata of [
    {NFT_DUMP_VERSION: '', NFT_DUMP_BYTES: '123456', NFT_DUMP_SHA256: 'a'.repeat(64)},
    {NFT_DUMP_VERSION: 'null', NFT_DUMP_BYTES: '123456', NFT_DUMP_SHA256: 'a'.repeat(64)},
    {NFT_DUMP_VERSION: 'seed-v1', NFT_DUMP_BYTES: '0', NFT_DUMP_SHA256: 'a'.repeat(64)},
    {NFT_DUMP_VERSION: 'seed-v1', NFT_DUMP_BYTES: '123456', NFT_DUMP_SHA256: 'not-a-digest'},
  ]) await t.test(JSON.stringify(metadata), async () => {
    const deps = freshDependencies();
    const definition = managedRestoreDefinition(11, metadata);
    deps.aws = async (service, operation, input = {}) => {
      if (operation === 'get-caller-identity') return {Account: '448120078528'};
      if (operation === 'list-task-definitions') return {taskDefinitionArns: [definition.taskDefinitionArn]};
      if (operation === 'describe-task-definition') return {taskDefinition: definition};
      throw new Error(`Unexpected ${service} ${operation}`);
    };

    await assert.rejects(resolveRestoreTaskDefinition({runId: config.runId}, deps), /dump identity/i);
    assert.equal(deps.state.taskDefinition, undefined);
  });
});

async function pausedMaintenanceSchedules({originallyDisabled} = {}) {
  const deps = dependencies();
  deps.state.maintenancePaused = false;
  if (originallyDisabled) {
    deps.schedules.set(originallyDisabled, {...deps.schedules.get(originallyDisabled), State: 'DISABLED'});
  }
  await lifecycle.snapshotMaintenanceSchedules(config, deps);
  await lifecycle.pauseMaintenanceSchedules(config, deps);
  return deps;
}

test('snapshots all seven validated NFT schedule configurations before pausing them', async () => {
  const deps = dependencies();
  deps.state.maintenancePaused = false;
  await lifecycle.snapshotMaintenanceSchedules(config, deps);

  assert.equal(deps.state.maintenanceScheduleSnapshot.length, 7);
  assert.deepEqual(deps.state.maintenanceScheduleSnapshot.map(schedule => schedule.Name).sort(),
    maintenanceScheduleDefinitions.map(([name]) => name).sort());
  assert.deepEqual(deps.state.maintenanceScheduleSnapshot[0].Target, deps.schedules.get(deps.state.maintenanceScheduleSnapshot[0].Name).Target);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-schedule'), false);

  await lifecycle.pauseMaintenanceSchedules(config, deps);
  const firstUpdate = deps.calls.findIndex(([operation]) => operation === 'update-schedule');
  const firstPrivateCheckpoint = deps.calls.findIndex(([operation]) => operation === 'checkpoint');
  assert.ok(firstPrivateCheckpoint >= 0 && firstPrivateCheckpoint < firstUpdate);
  assert.equal(deps.state.maintenancePaused, true);
  assert.ok([...deps.schedules.values()].every(schedule => schedule.State === 'DISABLED'));
});

test('schedule snapshot rejects an unexpected account, inventory, group, or target', async t => {
  for (const failure of ['account', 'inventory', 'group', 'target']) await t.test(failure, async () => {
    const deps = dependencies();
    deps.state.maintenancePaused = false;
    const original = deps.aws;
    if (failure === 'account') {
      deps.aws = async (service, operation, input) => operation === 'get-caller-identity'
        ? {Account: '000000000000'} : original(service, operation, input);
    } else if (failure === 'inventory') {
      deps.schedules.delete(maintenanceScheduleDefinitions[0][0]);
    } else if (failure === 'group') {
      const name = maintenanceScheduleDefinitions[0][0];
      deps.schedules.set(name, {...deps.schedules.get(name), GroupName: 'other-group'});
    } else {
      const name = maintenanceScheduleDefinitions[0][0];
      deps.schedules.set(name, {...deps.schedules.get(name), Target: {...deps.schedules.get(name).Target, Arn: 'arn:aws:ecs:eu-west-2:000000000000:cluster/other'}});
    }

    await assert.rejects(lifecycle.snapshotMaintenanceSchedules(config, deps));
    assert.equal(deps.calls.some(([operation]) => operation === 'update-schedule'), false);
    assert.equal(deps.state.maintenanceScheduleSnapshot, undefined);
  });
});

test('schedule pause preserves originally disabled jobs and drains already-running tasks', async () => {
  const deps = dependencies();
  deps.state.maintenancePaused = false;
  const originallyDisabled = maintenanceScheduleDefinitions[0][0];
  deps.schedules.set(originallyDisabled, {...deps.schedules.get(originallyDisabled), State: 'DISABLED'});
  await lifecycle.snapshotMaintenanceSchedules(config, deps);
  deps.state.scheduledWritersRunning = true;
  const original = deps.aws;
  let polls = 0;
  deps.aws = async (service, operation, input) => {
    if (operation === 'list-tasks' && input.cluster === 'nft-scheduled-tasks') {
      return {taskArns: ++polls < 3 ? ['scheduled-task'] : []};
    }
    return original(service, operation, input);
  };
  await lifecycle.pauseMaintenanceSchedules(config, deps);

  const updates = deps.calls.filter(([operation]) => operation === 'update-schedule');
  assert.equal(updates.length, 6);
  assert.equal(updates.some(([, input]) => input.Name === originallyDisabled), false);
  assert.equal(deps.schedules.get(originallyDisabled).State, 'DISABLED');
  assert.ok(polls >= 3);
  assert.equal(deps.state.maintenancePaused, true);
});

test('partial schedule pause persists ownership and resumes safely after an API failure', async () => {
  const deps = dependencies();
  deps.state.maintenancePaused = false;
  await lifecycle.snapshotMaintenanceSchedules(config, deps);
  const original = deps.aws;
  let updates = 0;
  deps.aws = async (service, operation, input) => {
    if (operation === 'update-schedule' && ++updates === 2) throw new Error('controlled schedule update failure');
    return original(service, operation, input);
  };

  await assert.rejects(lifecycle.pauseMaintenanceSchedules(config, deps), /controlled schedule update failure/);
  assert.equal(deps.state.maintenancePaused, false);
  assert.ok(Object.values(deps.state.maintenanceScheduleProgress).some(progress => progress.pauseAttempted));
  deps.aws = original;
  await lifecycle.pauseMaintenanceSchedules(config, deps);
  assert.equal(deps.state.maintenancePaused, true);
  assert.ok([...deps.schedules.values()].every(schedule => schedule.State === 'DISABLED'));
});

test('schedule drift after snapshot blocks pause before any update', async () => {
  const deps = dependencies();
  deps.state.maintenancePaused = false;
  await lifecycle.snapshotMaintenanceSchedules(config, deps);
  const name = maintenanceScheduleDefinitions[0][0];
  deps.schedules.set(name, {...deps.schedules.get(name), ScheduleExpression: 'rate(1 hour)'});

  await assert.rejects(lifecycle.pauseMaintenanceSchedules(config, deps), /changed|drift/i);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-schedule'), false);
});

test('writer shutdown refuses to proceed before maintenance isolation is complete', async () => {
  const deps = dependencies();
  deps.state.maintenancePaused = false;

  await assert.rejects(stopNftWriters(config, deps), /maintenance schedules must be paused and drained/i);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-service' || operation === 'run-task'), false);
});

test('cleanup restores a partial schedule pause only when destructive work never began', async () => {
  const deps = dependencies();
  const schedule = deps.schedules.get(maintenanceScheduleDefinitions[0][0]);
  deps.state.runId = config.runId;
  deps.state.taskDefinition = config.taskDefinition;
  deps.state.maintenancePaused = false;
  deps.state.maintenanceScheduleSnapshot = [...deps.schedules.values()].map(value => structuredClone(value));
  deps.state.maintenanceScheduleProgress = {[schedule.Name]: {pauseAttempted: true, paused: true}};
  deps.schedules.set(schedule.Name, {...schedule, State: 'DISABLED'});

  await cleanupNftEnvironment(config, async () => null, deps);

  assert.equal(deps.schedules.get(schedule.Name).State, 'ENABLED');
  assert.equal(deps.state.maintenanceRestored, true);
  assert.equal(deps.calls.some(([operation]) => operation === 'run-task' || operation === 'update-service'), false);
});

test('maintenance schedules resume to exact original states only after reset, QA, and async verification', async () => {
  const originallyDisabled = maintenanceScheduleDefinitions[0][0];
  const deps = await pausedMaintenanceSchedules({originallyDisabled});
  const original = new Map(deps.state.maintenanceScheduleSnapshot.map(schedule => [schedule.Name, schedule]));
  const updatesBeforeRecovery = deps.calls.filter(([operation]) => operation === 'update-schedule').length;

  await assert.rejects(lifecycle.restoreMaintenanceSchedules({
    ...config, resetVerified: false, recoveryVerified: false,
  }, deps), /verified database reset/i);
  assert.equal(deps.calls.filter(([operation]) => operation === 'update-schedule').length, updatesBeforeRecovery);
  assert.ok([...deps.schedules.values()].every(schedule => schedule.State === 'DISABLED'));

  Object.assign(deps.state, {resetVerified: true, qaVerified: true, asyncRestored: true, loadPassed: false});
  const recoveryConfig = {...config, resetVerified: true, recoveryVerified: true};
  await lifecycle.restoreMaintenanceSchedules(recoveryConfig, deps);
  for (const [name, schedule] of original) assert.deepEqual(deps.schedules.get(name), schedule);
  assert.equal(deps.state.maintenanceRestored, true);
  const updatesAfterRecovery = deps.calls.filter(([operation]) => operation === 'update-schedule').length;
  await lifecycle.restoreMaintenanceSchedules(recoveryConfig, deps);
  assert.equal(deps.calls.filter(([operation]) => operation === 'update-schedule').length, updatesAfterRecovery);
});

test('maintenance recovery leaves schedules paused when QA or async recovery is unverified', async () => {
  const deps = await pausedMaintenanceSchedules();
  Object.assign(deps.state, {resetVerified: true, asyncRestored: true});
  const updatesBeforeRecovery = deps.calls.filter(([operation]) => operation === 'update-schedule').length;

  await assert.rejects(lifecycle.restoreMaintenanceSchedules({
    ...config, resetVerified: true, recoveryVerified: true,
  }, deps), /verified database reset, application QA, and async recovery/i);
  assert.equal(deps.calls.filter(([operation]) => operation === 'update-schedule').length, updatesBeforeRecovery);
  assert.ok([...deps.schedules.values()].every(schedule => schedule.State === 'DISABLED'));
});

test('partial maintenance restoration retries without overwriting progress', async () => {
  const deps = await pausedMaintenanceSchedules();
  Object.assign(deps.state, {resetVerified: true, qaVerified: true, asyncRestored: true});
  const originalAws = deps.aws;
  let restores = 0;
  deps.aws = async (service, operation, input) => {
    if (operation === 'update-schedule' && input.State === 'ENABLED' && ++restores === 2) {
      throw new Error('controlled schedule restore failure');
    }
    return originalAws(service, operation, input);
  };
  const recoveryConfig = {...config, resetVerified: true, recoveryVerified: true};

  await assert.rejects(lifecycle.restoreMaintenanceSchedules(recoveryConfig, deps), /controlled schedule restore failure/);
  assert.equal(deps.state.maintenancePaused, true);
  assert.equal([...deps.schedules.values()].filter(schedule => schedule.State === 'ENABLED').length, 1);
  deps.aws = originalAws;
  await lifecycle.restoreMaintenanceSchedules(recoveryConfig, deps);
  assert.ok([...deps.schedules.values()].every(schedule => schedule.State === 'ENABLED'));
  assert.equal(deps.state.maintenanceRestored, true);
});

test('schedule configuration drift blocks restoration before any schedule is resumed', async () => {
  const deps = await pausedMaintenanceSchedules();
  Object.assign(deps.state, {resetVerified: true, qaVerified: true, asyncRestored: true});
  const updatesBeforeRecovery = deps.calls.filter(([operation]) => operation === 'update-schedule').length;
  const driftedName = maintenanceScheduleDefinitions.at(-1)[0];
  deps.schedules.set(driftedName, {...deps.schedules.get(driftedName), ScheduleExpression: 'rate(1 hour)'});

  await assert.rejects(lifecycle.restoreMaintenanceSchedules({
    ...config, resetVerified: true, recoveryVerified: true,
  }, deps), /drifted/i);
  assert.equal(deps.calls.filter(([operation]) => operation === 'update-schedule').length, updatesBeforeRecovery);
  assert.ok([...deps.schedules.values()].every(schedule => schedule.State === 'DISABLED'));
});

test('QA recovery marker requires an explicitly verified reset and restored app', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {runId: config.runId, taskDefinition: config.taskDefinition,
    maintenancePaused: true, appRestored: true});
  await assert.rejects(lifecycle.markQaVerified({...config, resetVerified: false}, deps), /verified post-reset app state/i);
  await lifecycle.markQaVerified({...config, resetVerified: true}, deps);
  assert.equal(deps.state.qaVerified, true);
});

test('records async ownership before stopping it and confirms tasks stopped before restore', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  const updateIndex = deps.calls.findIndex(([op, input]) => op === 'update-service' && input.service === 'nft-async-worker');
  assert.ok(updateIndex > 0);
  assert.deepEqual(deps.calls[updateIndex - 1][1].originalAsync, {taskDefinition: asyncArn, desiredCount: 1});
  assert.deepEqual(deps.state.asyncTasks, ['async-task']);
  await restoreInVpc(config, deps);
  assert.ok(deps.calls.findIndex(([op]) => op === 'run-task') > updateIndex);
});

test('restores exactly the owned async service and rejects missing or changed state', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  await restoreAsyncWorker(config, deps);
  assert.deepEqual(deps.calls.filter(([op, input]) => op === 'update-service' && input.service === 'nft-async-worker')
    .map(([, input]) => input.desiredCount), [0, 1]);
  const missing = dependencies();
  await assert.rejects(restoreAsyncWorker(config, missing), /state/);
  const changed = dependencies();
  Object.assign(changed.state, {runId: config.runId, taskDefinition: workerArn,
    originalAsync: {taskDefinition: `${asyncArn}-other`, desiredCount: 1}, asyncStopAttempted: true});
  await assert.rejects(restoreAsyncWorker(config, changed), /changed|not runnable/);
  assert.equal(changed.calls.some(([op]) => op === 'update-service'), false);
});

test('preserves a previously stopped async service', async () => {
  const deps = dependencies();
  const original = deps.aws;
  deps.aws = async (service, operation, input) => {
    if (operation === 'describe-services' && input.services.includes('nft-async-worker')) {
      return {services: [{...asyncWorker(), desiredCount: 0, runningCount: 0}]};
    }
    if (operation === 'list-tasks' && input.serviceName === 'nft-async-worker') return {taskArns: []};
    return original(service, operation, input);
  };
  await stopNftWriters(config, deps);
  await restoreAsyncWorker(config, deps);
  assert.equal(deps.state.originalAsync.desiredCount, 0);
  assert.equal(deps.calls.some(([op, input]) => op === 'update-service' && input.service === 'nft-async-worker' && input.desiredCount !== 0), false);
});

test('recovers the async service after a lost stop response using persisted ownership', async () => {
  const deps = dependencies();
  const original = deps.aws;
  let lost = false;
  deps.aws = async (service, operation, input) => {
    const result = await original(service, operation, input);
    if (!lost && operation === 'update-service' && input.service === 'nft-async-worker') {
      lost = true;
      throw new Error('stop response lost');
    }
    return result;
  };
  await assert.rejects(stopNftWriters(config, deps), /stop response lost/);
  assert.equal(deps.state.asyncStopAttempted, true);
  const persisted = deps.calls.filter(([op]) => op === 'persist').at(-1)[1];
  assert.deepEqual(persisted.originalAsync, {taskDefinition: asyncArn, desiredCount: 1});
  await stopNftWriters(config, deps);
  await restoreAsyncWorker(config, deps);
  assert.equal(deps.state.asyncRestored, true);
});

test('does not launch a restore while a recorded async task is still running', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  const original = deps.aws;
  deps.aws = async (service, operation, input) =>
    operation === 'describe-tasks' && input.tasks.includes('async-task')
      ? {tasks: [{taskArn: 'async-task', lastStatus: 'DEACTIVATING'}]}
      : original(service, operation, input);
  await assert.rejects(restoreInVpc(config, deps), /async writer/);
  assert.equal(deps.calls.some(([op]) => op === 'run-task'), false);
});

test('workflow attempts async recovery independently after a successful reset', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const job = workflow.split('  restore-async-after-database-reset:')[1]?.split('\n  restore-maintenance-schedules:')[0];
  assert.ok(job);
  assert.match(job, /always\(\)/);
  assert.match(job, /needs\.reset-database\.result == 'success'/);
  assert.match(job, /needs\.restore-app-after-database-reset\.result == 'success'/);
  assert.match(job, /nft-vpc-restore\.mjs restore-async/);
});

test('workflow resolves one active restore revision and reuses it without an environment pin', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const restore = workflow.split('  restore-dataset:')[1]?.split('\n  performance-tests:')[0];

  assert.ok(restore);
  assert.match(restore, /id: resolve_restore[\s\S]*nft-vpc-restore\.mjs resolve-task-definition[\s\S]*nft-vpc-restore\.mjs snapshot-maintenance/);
  const dispatch = workflow.split('  workflow_dispatch:')[1]?.split('\nconcurrency:')[0];
  assert.ok(dispatch);
  assert.match(dispatch, /restore-task-definition:[\s\S]*?default: ''/);
  assert.match(restore, /NFT_RESTORE_TASK_DEFINITION: \$\{\{ inputs\.restore-task-definition \}\}/);
  assert.match(restore, /restore-task-definition: \$\{\{ steps\.resolve_restore\.outputs\.task_definition \}\}/);
  const publishIndex = restore.indexOf('name: Publish resolved restore task definition to this job');
  const snapshotIndex = restore.indexOf('nft-vpc-restore.mjs snapshot-maintenance');
  assert.ok(publishIndex >= 0 && publishIndex < snapshotIndex,
    'the resolved revision must be exported before the snapshot consumes it');
  const publishStep = restore.slice(publishIndex, snapshotIndex);
  assert.match(publishStep, /NFT_RESTORE_TASK_DEFINITION: \$\{\{ steps\.resolve_restore\.outputs\.task_definition \}\}/);
  assert.match(publishStep, /GITHUB_ENV/);
  assert.match(workflow, /NFT_RESTORE_TASK_DEFINITION: \$\{\{ needs\.restore-dataset\.outputs\.restore-task-definition \}\}/);
  assert.doesNotMatch(workflow, /vars\.NFT_RESTORE_TASK_DEFINITION/);
  assert.match(workflow, /if: github\.ref == 'refs\/heads\/nft'/);
  assert.doesNotMatch(workflow, /refs\/heads\/feat\/PDJB-553-complete-performance-coverage/);
});

test('does not restart the baseline worker after reset until app migration and QA are verified', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  await restoreInVpc(config, deps);

  await assert.rejects(restoreAsyncWorker({...config, resetVerified: true}, deps), /QA verification/i);

  assert.equal(deps.calls.some(([operation, input]) =>
    operation === 'update-service' && input.service === 'nft-async-worker' && input.desiredCount > 0), false);
});

test('workflow restores maintenance after recovery even when the performance load fails', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const job = workflow.split('  restore-maintenance-schedules:')[1]?.split('\n  maintenance-recovery-required:')[0];
  assert.ok(job);
  assert.match(job, /always\(\)/);
  assert.match(job, /needs\.reset-database\.result == 'success'/);
  assert.match(job, /needs\.restore-app-after-database-reset\.result == 'success'/);
  assert.match(job, /needs\.restore-async-after-database-reset\.result == 'success'/);
  assert.doesNotMatch(job, /needs\.performance-tests\.result == 'success'/);
  assert.match(job, /nft-vpc-restore\.mjs restore-maintenance/);
});

test('workflow gates every upload phase on candidate worker readiness and checkpoints its partial changes', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const job = workflow.split('  performance-tests:')[1].split('\n  stop-writers-for-reset:')[0];
  const worker = job.indexOf('nft-vpc-restore.mjs start-async');
  assert.ok(worker > job.indexOf('nft-vpc-restore.mjs start'));
  assert.ok(worker < job.indexOf('nft-prepare.mjs'));
  assert.match(job, /if: always\(\)\s+run: node scripts\/performance-tests\/nft-recovery-state\.mjs upload worker-state/);
});

test('compatibility task definition uses a variable with a legacy secret fallback', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  assert.match(workflow, /NFT_RESET_COMPATIBLE_ASYNC_TASK_DEFINITION: \$\{\{ vars\.NFT_RESET_COMPATIBLE_ASYNC_TASK_DEFINITION \|\| secrets\.NFT_RESET_COMPATIBLE_ASYNC_TASK_DEFINITION \}\}/);
});

test('candidate scan worker cannot start from an unverified restore or silently resume the old image', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  await assert.rejects(() => lifecycle.startAsyncWorker({...config, expectedSha: 'a'.repeat(40)}, deps),
    /Successful database restore/);
  assert.equal(deps.calls.some(([op]) => op === 'register-task-definition'), false);
});

async function candidateDependencies({baselineCount = 1, loseSwitch = false, loseRegistration = false, lifecycleConfig = config} = {}) {
  const deps = dependencies();
  const baselineAws = deps.aws;
  const appImage = `448120078528.dkr.ecr.eu-west-2.amazonaws.com/prsdb-webapp:candidate-${'a'.repeat(40)}`;
  let appStopped = false;
  let asyncStopped = baselineCount === 0;
  deps.aws = async (service, operation, input = {}) => {
    if (baselineCount === 0 && operation === 'describe-services' && input.services.includes('nft-async-worker')) {
      return {services: [{...asyncWorker(), desiredCount: 0, runningCount: 0}]};
    }
    if (operation === 'describe-task-definition' && input.taskDefinition === appArn) {
      return {taskDefinition: {taskDefinitionArn: appArn, family: 'prsdb-webapp-nft', status: 'ACTIVE',
        containerDefinitions: [{name: 'prsdb-webapp', image: appImage}]}};
    }
    if (operation === 'describe-task-definition' && input.taskDefinition === asyncArn) {
      return {taskDefinition: {taskDefinitionArn: asyncArn, family: 'prsdb-async-worker-nft', status: 'ACTIVE',
        containerDefinitions: [{name: 'prsdb-async-worker', image: appImage}]}};
    }
    if (operation === 'describe-tasks' && input.tasks.includes('app-task')) {
      return {tasks: [{taskArn: 'app-task', taskDefinitionArn: appArn,
        lastStatus: appStopped ? 'STOPPED' : 'RUNNING',
        containers: [{name: 'prsdb-webapp', image: appImage, imageDigest}]}]};
    }
    if (operation === 'describe-tasks' && input.tasks.includes('async-task')) {
      return {tasks: [{taskArn: 'async-task', taskDefinitionArn: asyncArn,
        lastStatus: asyncStopped ? 'STOPPED' : 'RUNNING',
        containers: [{name: 'prsdb-async-worker', image: appImage, imageDigest}]}]};
    }
    if (operation === 'update-service') {
      if (input.service === 'nft-async-worker') asyncStopped = input.desiredCount === 0;
      else appStopped = input.desiredCount === 0;
    }
    return baselineAws(service, operation, input);
  };
  await stopNftWriters(lifecycleConfig, deps);
  deps.state.restorePassed = true;
  const original = deps.aws;
  const candidateArn = `${prefix}task-definition/prsdb-async-worker-nft-performance-123-1:1`;
  let revision = asyncArn;
  let desired = 0;
  let appDesired = 1;
  let registered;
  const env = [
    {name: 'RDS_URL', value: 'jdbc:postgresql://nft-db/database'}, {name: 'RDS_USERNAME', value: 'nft-user'},
    {name: 'AWS_QUARANTINE_BUCKET', value: 'prsdb-quarantine-nft'}, {name: 'S3_SAFE_BUCKET_KEY', value: 'prsdb-uploaded-files-nft'},
  ];
  const definition = worker => ({
    taskDefinitionArn: worker ? asyncArn : appArn, family: worker ? 'prsdb-async-worker-nft' : 'prsdb-webapp-nft',
    status: 'ACTIVE', taskRoleArn: 'arn:aws:iam::448120078528:role/nft-async-worker-ecs-task',
    executionRoleArn: 'arn:aws:iam::448120078528:role/nft-ecs-task-execution',
    containerDefinitions: [{
      name: worker ? 'prsdb-async-worker' : 'prsdb-webapp', essential: true,
      image: appImage,
      environment: [...env, {name: 'SPRING_PROFILES_ACTIVE', value: worker ? 'web-server-deactivated,email-worker,nft' : 'default,nft'}],
      secrets: [{name: 'RDS_PASSWORD', valueFrom: 'nft-password'}],
    }],
  });
  deps.aws = async (service, operation, input = {}) => {
    deps.calls.push([operation, structuredClone(input)]);
    if (operation === 'describe-services') {
      if (input.services.includes('nft-async-worker')) return {services: [{
        ...asyncWorker(), taskDefinition: revision, desiredCount: desired, runningCount: desired,
        deployments: [{taskDefinition: revision, rolloutState: 'COMPLETED'}],
      }]};
      return {services: [{...app(), desiredCount: appDesired, runningCount: appDesired}, simulator]};
    }
    if (operation === 'describe-task-definition') {
      if (input.taskDefinition === candidateArn) return {taskDefinition: {...registered, taskDefinitionArn: candidateArn, status: 'ACTIVE'}};
      if (input.taskDefinition === workerArn) return {taskDefinition: managedRestoreDefinition(3)};
      return {taskDefinition: definition(input.taskDefinition === asyncArn)};
    }
    if (operation === 'register-task-definition') {
      registered = structuredClone(input);
      if (loseRegistration) throw new Error('Controlled lost registration response');
      return {taskDefinition: {taskDefinitionArn: candidateArn}};
    }
    if (operation === 'list-task-definitions') return {taskDefinitionArns: input.familyPrefix.includes('async-worker') ? [candidateArn] : [appArn]};
    if (operation === 'update-service') {
      if (input.service === 'nft-async-worker') {
        revision = input.taskDefinition ?? revision;
        desired = input.desiredCount;
        if (loseSwitch && desired === 1 && revision === candidateArn) throw new Error('Controlled lost switch response');
      } else appDesired = input.desiredCount;
      return {};
    }
    if (operation === 'describe-tasks' && input.tasks.includes('async-task')) return {tasks: [{
      taskArn: 'async-task', taskDefinitionArn: revision, lastStatus: desired ? 'RUNNING' : 'STOPPED',
      containers: [{name: 'prsdb-async-worker', lastStatus: desired ? 'RUNNING' : 'STOPPED', imageDigest}],
    }]};
    if (operation === 'describe-tasks' && input.tasks.includes('app-task')) return {tasks: [{
      taskArn: 'app-task', taskDefinitionArn: appArn, lastStatus: appDesired ? 'RUNNING' : 'STOPPED',
      containers: [{name: 'prsdb-webapp', image: definition(false).containerDefinitions[0].image, imageDigest}],
    }]};
    if (operation === 'get-parameter') return {Parameter: {Value: 'nft-prsdb-scan-result-queue'}};
    if (operation === 'list-services') return {serviceArns: ['nft-app', 'nft-one-login-simulator', 'nft-async-worker']
      .map(name => `${prefix}service/nft-app/${name}`)};
    if (operation === 'deregister-task-definition') return {};
    return original(service, operation, input);
  };
  return {...deps, candidateArn};
}

test('candidate worker is ready throughout uploads, quiesces before recovery, and preserves an originally stopped baseline', async () => {
  const unpinnedConfig = {...config, expectedSha: 'a'.repeat(40)};
  delete unpinnedConfig.resetCompatibleTaskDefinition;
  delete unpinnedConfig.resetCompatibleSha;
  const deps = await candidateDependencies({baselineCount: 0, lifecycleConfig: unpinnedConfig});
  assert.equal(deps.state.originalAsync.desiredCount, 0);
  await lifecycle.startAsyncWorker(unpinnedConfig, deps);
  assert.equal(deps.state.asyncReady, true);
  assert.equal(deps.state.writersStopped, false);
  assert.equal(deps.calls.find(([op]) => op === 'register-task-definition')[1].containerDefinitions[0].image,
    `448120078528.dkr.ecr.eu-west-2.amazonaws.com/prsdb-webapp@${imageDigest}`);
  let quiescentBeforeReadiness = false;
  await cleanupNftEnvironment(unpinnedConfig, async () => {
    quiescentBeforeReadiness = deps.state.asyncQuiescent;
    return null;
  }, deps);
  assert.equal(quiescentBeforeReadiness, true);
  assert.equal(deps.state.asyncRetired, true);
  await assert.rejects(restoreAsyncWorker(unpinnedConfig, deps), /Verified database reset/);
  markQaVerified(deps);
  await restoreAsyncWorker({...unpinnedConfig, resetVerified: true}, deps);
  const updates = deps.calls.filter(([op, input]) => op === 'update-service' && input.service === 'nft-async-worker');
  assert.deepEqual(updates.at(-1)[1], {cluster: 'nft-app', service: 'nft-async-worker', desiredCount: 0});
  assert.deepEqual(deps.calls.filter(([op]) => op === 'deregister-task-definition'),
    [['deregister-task-definition', {taskDefinition: deps.candidateArn}]]);
});

test('lost candidate registration or service-switch responses retain sufficient private ownership for recovery', async t => {
  for (const failure of ['loseRegistration', 'loseSwitch']) await t.test(failure, async () => {
    const workflowConfig = {...config, expectedSha: 'a'.repeat(40)};
    const deps = await candidateDependencies({[failure]: true, lifecycleConfig: workflowConfig});
    await assert.rejects(lifecycle.startAsyncWorker(workflowConfig, deps), /Controlled lost/);
    await cleanupNftEnvironment(workflowConfig, async () => null, deps);
    markQaVerified(deps);
    await restoreAsyncWorker({...workflowConfig, resetVerified: true}, deps);
    assert.equal(deps.state.asyncRetired, true);
    assert.equal(deps.state.asyncRestored, true);
    const updates = deps.calls.filter(([op, input]) => op === 'update-service' && input.service === 'nft-async-worker');
    assert.equal(updates.at(-1)[1].desiredCount, 1);
    assert.equal(deps.calls.some(([op, input]) => op === 'deregister-task-definition' && input.taskDefinition === asyncArn), false);
  });
});

  test('candidate worker fails closed before registration for a wrong queue or unverified app digest', async t => {
    for (const failure of ['queue', 'digest']) await t.test(failure, async () => {
      const deps = await candidateDependencies();
      const original = deps.aws;
      deps.aws = async (service, operation, input) => {
        if (failure === 'queue' && operation === 'get-parameter') return {Parameter: {Value: 'production-queue'}};
        if (failure === 'digest' && operation === 'describe-tasks' && input.tasks.includes('app-task')) {
          return {tasks: [{taskDefinitionArn: appArn, lastStatus: 'RUNNING', containers: [{name: 'prsdb-webapp'}]}]};
        }
        return original(service, operation, input);
      };
      await assert.rejects(lifecycle.startAsyncWorker({...config, expectedSha: 'a'.repeat(40)}, deps), /queue is not ready|digest is unverified/);
      assert.equal(deps.calls.some(([op]) => op === 'register-task-definition'), false);
    });
  });

  test('cleanup waits for queued work with candidate still running and never purges messages', async () => {
    const workflowConfig = {...config, expectedSha: 'a'.repeat(40)};
    const deps = await candidateDependencies({lifecycleConfig: workflowConfig});
    await lifecycle.startAsyncWorker(workflowConfig, deps);
    const original = deps.aws;
    let samples = 0;
    const start = deps.calls.length;
    deps.aws = async (service, operation, input) => {
      const result = await original(service, operation, input);
      if (operation === 'get-queue-attributes' && ++samples < 3) {
        assert.equal(deps.calls.slice(start).some(([op, value]) => op === 'update-service' && value.service === 'nft-async-worker'), false);
        result.Attributes.ApproximateNumberOfMessagesNotVisible = '1';
      }
      return result;
    };
    await cleanupNftEnvironment(workflowConfig, async () => null, deps);
    assert.ok(samples >= 3);
    assert.equal(deps.calls.some(([op]) => /purge|delete-message|receive-message/.test(op)), false);
  });

  test('cleanup blocks destructive recovery for a draining task or an unrecognized service reference', async t => {
    for (const failure of ['draining', 'reference']) await t.test(failure, async () => {
      const deps = await candidateDependencies();
      let clock = 0;
      Object.assign(deps, {now: () => clock, timeoutMs: 1, pollMs: 1, sleep: async () => { clock++; }});
      await lifecycle.startAsyncWorker({...config, expectedSha: 'a'.repeat(40)}, deps);
      const original = deps.aws;
      deps.aws = async (service, operation, input) => {
        if (failure === 'draining' && operation === 'describe-tasks' && input.tasks.includes('async-task')) {
          return {tasks: [{lastStatus: 'DEACTIVATING'}]};
        }
        if (failure === 'reference' && operation === 'list-services') return {serviceArns: ['unexpected-service']};
        return original(service, operation, input);
      };
      let readinessTouched = false;
      await assert.rejects(cleanupNftEnvironment(config, async () => { readinessTouched = true; return null; }, deps),
        /shutdown timeout|references cannot be established/);
      assert.equal(readinessTouched, false);
      assert.equal(deps.calls.some(([op]) => op === 'deregister-task-definition'), false);
    });
  });

  test('abandonment recovers candidate identity even if the last worker checkpoint was lost', async () => {
      const workflowConfig = {...config, expectedSha: 'a'.repeat(40)};
      const deps = await candidateDependencies({lifecycleConfig: workflowConfig});
      await lifecycle.startAsyncWorker(workflowConfig, deps);
    for (const key of ['asyncRegistrationAttempted', 'candidateAsync', 'asyncImage', 'asyncSwitchAttempted', 'asyncReady']) delete deps.state[key];
      await cleanupNftEnvironment(workflowConfig, async () => null, deps);
    assert.equal(deps.state.asyncRetired, true);
      markQaVerified(deps);
      const compatible = deps.state.resetCompatibility;
      deps.state.resetCompatibility = {...compatible, compatibilitySha: 'd'.repeat(40)};
      await assert.rejects(restoreAsyncWorker({...workflowConfig, resetVerified: true}, deps),
        /compatibility is not verified/);
      deps.state.resetCompatibility = compatible;
      await restoreAsyncWorker({...workflowConfig, resetVerified: true}, deps);
    assert.equal(deps.state.asyncRestored, true);
  });

test('waits for actual app tasks STOPPED even after ECS reports zero running', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  const polls = deps.calls.filter(([operation, input]) => operation === 'describe-tasks' && input.tasks.includes('app-task'));
  assert.equal(polls.length, 2);
  assert.ok(deps.calls.find(([operation, state]) => operation === 'persist' && state.originalApp)?.[1].originalApp);
  assert.equal(deps.state.writersStopped, true);
});

test('attempts readiness cleanup even when stopping the restore worker fails and never resets', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {runId: config.runId, taskDefinition: workerArn, launchAttempted: true});
  let ownedAccessChecked = false;
  deps.aws = async (service, operation) => {
    deps.calls.push([operation]);
    if (operation === 'get-caller-identity') return {Account: '448120078528'};
    if (operation === 'get-ip-set') {
      ownedAccessChecked = true;
      return {IPSet: {Addresses: []}};
    }
    throw new Error('worker lookup failed');
  };
  await assert.rejects(cleanupNftEnvironment(config, async () => {
    return {ownedSets: [{input: {Id: 'owned-set'}, wafRegion: 'eu-west-2'}], cidr: '203.0.113.1/32'};
  }, deps), /Cleanup incomplete/);
  assert.equal(ownedAccessChecked, true);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
});

test('can safely reset after app startup fails before preparation has changed anything', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {runId: config.runId, taskDefinition: workerArn});
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'list-task-definitions'
    ? {taskDefinitionArns: [appArn]} : original(service, operation, input);
  await cleanupNftEnvironment(config, async () => null, deps);
  assert.equal(deps.state.writersStopped, true);
});

test('unreadable preparation state prevents reset but still attempts worker shutdown', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {
    runId: config.runId, taskDefinition: workerArn, launchAttempted: true, taskArn,
  });
  await assert.rejects(cleanupNftEnvironment(config, async () => {
    throw new Error('state unreadable');
  }, deps), /Cleanup incomplete/);
  assert.equal(deps.calls.some(([operation]) => operation === 'describe-tasks'), true);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
});

test('rejects scheduled writers before stopping the app', async () => {
  const deps = dependencies();
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'list-tasks' && input.cluster === 'nft-scheduled-tasks'
    ? {taskArns: ['scheduled-writer']} : original(service, operation, input);
  await assert.rejects(stopNftWriters(config, deps), /Scheduled NFT writers/);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
});

test('rejects a retired restore worker before stopping the healthy app', async () => {
  const deps = dependencies();
  const initialState = structuredClone(deps.state);
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'describe-task-definition' && input.taskDefinition === workerArn
    ? {taskDefinition: {taskDefinitionArn: workerArn, status: 'INACTIVE'}}
    : original(service, operation, input);
  await assert.rejects(stopNftWriters(config, deps), /active managed NFT restore task definition/);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
  assert.deepEqual(deps.state, initialState);
});

test('rejects a workflow SHA that is not deployed before app shutdown', async () => {
  const deps = dependencies();
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'describe-task-definition' &&
    input.taskDefinition === appArn
    ? {taskDefinition: {taskDefinitionArn: appArn, status: 'ACTIVE',
      containerDefinitions: [{name: 'prsdb-webapp', image: `nft-webapp:v0.1.0-${'a'.repeat(40)}`}]}}
    : original(service, operation, input);
  await assert.rejects(stopNftWriters({...config, expectedSha: 'b'.repeat(40)}, deps), /Workflow SHA is not deployed/);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
});

test('matching deployed app and worker images recover without per-run compatibility pins', async () => {
  const sha = 'b'.repeat(40);
  const deps = deployedCompatibilityDependencies({sha});

  await stopNftWriters({...config, expectedSha: sha}, deps);

  assert.equal(deps.state.resetCompatibility.mode, 'matching-workflow');
  assert.equal(deps.state.resetCompatibility.appSha, sha);
  assert.equal(deps.state.resetCompatibility.appImageDigest, imageDigest);
  assert.equal(deps.state.resetCompatibility.asyncImageDigest, imageDigest);
  assert.equal(deps.state.writersStopped, true);
});

test('mismatched running app and worker digests fail before stopping either service', async () => {
  const sha = 'b'.repeat(40);
  const deps = deployedCompatibilityDependencies({sha, workerDigest: `sha256:${'d'.repeat(64)}`});

  await assert.rejects(stopNftWriters({...config, expectedSha: sha}, deps), /running image digests do not match/i);

  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
  assert.equal(deps.state.stopAttempted, undefined);
});

test('different deployed and reset SHAs require the explicit compatibility exception', async () => {
  const deps = deployedCompatibilityDependencies({sha: 'a'.repeat(40)});
  const unpinned = {...config, expectedSha: 'a'.repeat(40)};
  delete unpinned.resetCompatibleTaskDefinition;
  delete unpinned.resetCompatibleSha;

  await assert.rejects(stopNftWriters(unpinned, deps),
    /explicit reset compatibility pins/i);

  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
});

test('launches one private worker only after shutdown and persists its ARN before monitoring', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  await restoreInVpc(config, deps);
  const launch = deps.calls.find(([operation]) => operation === 'run-task')[1];
  assert.equal(launch.taskDefinition, workerArn);
  assert.equal(launch.count, 1);
  assert.equal(launch.networkConfiguration.awsvpcConfiguration.assignPublicIp, 'DISABLED');
  const launchIndex = deps.calls.findIndex(([operation]) => operation === 'run-task');
  assert.equal(deps.calls[launchIndex + 1][0], 'persist');
  assert.equal(deps.calls[launchIndex + 1][1].taskArn, taskArn);
  assert.equal(deps.state.restorePassed, true);
});

test('certificate preflight rechecks the pinned version immediately before launch and fails closed', async () => {
  const deps = dependencies();
  await resolveRestoreTaskDefinition(config, deps);
  await stopNftWriters(config, deps);
  const original = deps.aws;
  deps.aws = async (service, operation, input) => {
    if (operation === 'head-object') {
      deps.calls.push([operation, structuredClone(input)]);
      return {VersionId: 'other', ContentLength: 42};
    }
    return original(service, operation, input);
  };
  await assert.rejects(restoreInVpc(config, deps), /certificate/i);
  assert.equal(deps.state.launchAttempted, undefined);
  assert.equal(deps.calls.some(([operation]) => operation === 'run-task'), false);
  const heads = deps.calls.filter(([operation]) => operation === 'head-object');
  assert.equal(heads.length, 2);
  assert.deepEqual(heads[0][1], heads[1][1]);
});

test('rejects failed or missing essential container exit codes', async () => {
  for (const exitCode of [1, undefined]) {
    const deps = dependencies();
    await stopNftWriters(config, deps);
    const original = deps.aws;
    deps.aws = async (service, operation, input) => operation === 'describe-tasks' && input.tasks.includes(taskArn)
      ? {tasks: [{taskArn, taskDefinitionArn: workerArn, lastStatus: 'STOPPED', containers: [{name: 'nft-vpc-restore', exitCode}]}]}
      : original(service, operation, input);
    await assert.rejects(restoreInVpc(config, deps), /essential container/);
    assert.notEqual(deps.state.restorePassed, true);
  }
});

test('refuses another AWS account or task family before stopping writers', async () => {
  const deps = dependencies();
  await assert.rejects(stopNftWriters({...config, taskDefinition: `${prefix}task-definition/prsdb-webapp-nft:95`}, deps), /restore task definition/);
  assert.equal(deps.calls.length, 0);
  deps.aws = async () => ({Account: '000000000000'});
  await assert.rejects(stopNftWriters(config, deps), /Wrong NFT AWS account/);
});

test('persists launch intent and leaves recovery identifiers when RunTask response is lost', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  const original = deps.aws;
  deps.aws = async (service, operation, input) => {
    if (operation === 'run-task') throw new Error('AWS response lost');
    return original(service, operation, input);
  };
  await assert.rejects(restoreInVpc(config, deps), /AWS response lost/);
  assert.equal(deps.state.launchAttempted, true);
  assert.match(deps.state.startedBy, /^nft-restore-123-1$/);
  assert.ok(deps.state.clientToken);
});

test('recovers a lost launch response by run marker and stops only the matching task', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {launchAttempted: true, startedBy: 'nft-restore-123-1', taskDefinition: workerArn});
  let stopped = false;
  deps.aws = async (service, operation, input) => {
    deps.calls.push([operation, input]);
    if (operation === 'list-tasks') return {taskArns: [taskArn]};
    if (operation === 'stop-task') { stopped = true; return {}; }
    return {tasks: [{taskArn, taskDefinitionArn: workerArn, startedBy: deps.state.startedBy, lastStatus: stopped ? 'STOPPED' : 'RUNNING'}]};
  };
  await stopRestoreTask(deps);
  assert.equal(deps.state.taskArn, taskArn);
  assert.deepEqual(deps.calls.find(([operation]) => operation === 'stop-task')[1], {
    cluster: 'nft-app', task: taskArn, reason: 'GitHub NFT restore cleanup',
  });
});

test('will not stop an unrelated task discovered during lost-response recovery', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {launchAttempted: true, startedBy: 'nft-restore-123-1', taskDefinition: workerArn});
  deps.aws = async (service, operation, input) => {
    deps.calls.push([operation, input]);
    if (operation === 'list-tasks') return {taskArns: [taskArn]};
    return {tasks: [{taskArn, taskDefinitionArn: workerArn, startedBy: 'someone-else'}]};
  };
  await assert.rejects(stopRestoreTask(deps), /does not belong/);
  assert.equal(deps.calls.some(([operation]) => operation === 'stop-task'), false);
});

test('blocks reset when a lost launch response cannot yet be reconciled', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {launchAttempted: true, startedBy: 'nft-restore-123-1', taskDefinition: workerArn});
  deps.aws = async () => ({taskArns: []});
  await assert.rejects(stopRestoreTask(deps), /Cannot prove whether the restore task started/);
});

test('does not re-query an already verified stopped restore after ECS forgets it', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {
    launchAttempted: true, taskArn, taskDefinition: workerArn, restorePassed: true, workerPassed: true,
    containers: [{name: 'nft-vpc-restore', exitCode: 0}],
  });
  deps.aws = async () => { throw new Error('ECS has forgotten the stopped task'); };
  await stopRestoreTask(deps);
});

test('incomplete or failed completion evidence cannot bypass task lookup', async () => {
  for (const containers of [undefined, [], [{name: 'nft-vpc-restore', exitCode: 1}], [{name: 'other', exitCode: 0}]]) {
    const deps = dependencies();
    Object.assign(deps.state, {launchAttempted: true, taskArn, taskDefinition: workerArn, restorePassed: true, workerPassed: true, containers});
    deps.aws = async () => ({tasks: [], failures: [{arn: taskArn, reason: 'MISSING'}]});
    await assert.rejects(stopRestoreTask(deps), /NFT task lookup failed/);
  }
});

test('restore flag alone cannot bypass lookup without verified worker completion', async () => {
  const deps = dependencies();
  Object.assign(deps.state, {
    launchAttempted: true, taskArn, taskDefinition: workerArn, restorePassed: true,
    containers: [{name: 'nft-vpc-restore', exitCode: 0}],
  });
  deps.aws = async () => ({tasks: [], failures: [{arn: taskArn, reason: 'MISSING'}]});
  await assert.rejects(stopRestoreTask(deps), /NFT task lookup failed/);
});

test('startup requires completed rollout and a healthy ALB target matching the actual app task', async () => {
  let clock = 0;
  const deps = dependencies({now: () => clock, timeoutMs: 100, sleep: async () => { clock += 100; }});
  Object.assign(deps.state, {originalApp: {taskDefinition: appArn}, restorePassed: true});
  let healthChecks = 0;
  deps.aws = async (service, operation) => {
    if (operation === 'get-caller-identity') return {Account: '448120078528'};
    if (operation === 'describe-services') return {services: [
      {...app(), loadBalancers: [{targetGroupArn: 'approved-target-group'}]}, simulator,
    ]};
    if (operation === 'list-tasks') return {taskArns: ['app-task']};
    if (operation === 'describe-tasks') return {tasks: [{containers: [{networkInterfaces: [{privateIpv4Address: '10.1.0.10'}]}]}]};
    if (operation === 'describe-target-health') {
      healthChecks++;
      return {TargetHealthDescriptions: [{
        Target: {Id: healthChecks === 1 ? '10.1.0.11' : '10.1.0.10'}, TargetHealth: {State: 'healthy'},
      }]};
    }
    return {};
  };
  await startNftApp(deps);
  assert.equal(healthChecks, 2);
});

test('a different recovery runner can check ALB health without using the previous runners WAF allowlist', async () => {
  const deps = dependencies();
  let probes = 0;
  await restoreEnvironment({
    originalApp: {taskDefinition: appArn, desiredCount: 1}, appSwitchAttempted: true, ownedSets: [],
  }, {
    ...deps,
    fetchImpl: async () => { throw new Error('Public HTTP is blocked for the recovery runner'); },
    appHealthy: async () => ++probes === 2,
  });
  assert.equal(probes, 2);
});

test('stops only the recorded worker task when its polling deadline expires', async () => {
  let clock = 0;
  const deps = dependencies({now: () => clock, timeoutMs: 100, sleep: async () => { clock += 100; }});
  await stopNftWriters(config, deps);
  const original = deps.aws;
  let stopRequested = false;
  deps.aws = async (service, operation, input) => {
    if (operation === 'stop-task') { deps.calls.push([operation, input]); stopRequested = true; return {}; }
    if (operation === 'describe-tasks' && input.tasks.includes(taskArn)) {
      return {tasks: [{taskArn, taskDefinitionArn: workerArn, lastStatus: stopRequested ? 'STOPPED' : 'RUNNING'}]};
    }
    return original(service, operation, input);
  };
  await assert.rejects(restoreInVpc(config, deps), /timeout/);
  const stops = deps.calls.filter(([operation]) => operation === 'stop-task');
  assert.deepEqual(stops, [['stop-task', {cluster: 'nft-app', task: taskArn, reason: 'GitHub NFT restore cleanup'}]]);
  assert.equal(stopRequested, true);
});

test('manual NFT reset and verification use the NFT revision rather than the dispatch branch', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/reset-database-nft.yml', import.meta.url), 'utf8');
  assert.doesNotMatch(workflow, /checkout-ref:/);
  assert.match(workflow.split('  verify-qa:')[1], /actions\/checkout@v7\s+with:\s+ref: nft/);
});

test('workflow separates reusable classes from versioned recovery checkpoints and fresh workloads', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const restore = workflow.split('  restore-dataset:')[1].split('\n  performance-tests:')[0];
  const scheduleSnapshot = restore.indexOf('nft-vpc-restore.mjs snapshot-maintenance');
  const scheduleCheckpoint = restore.indexOf('nft-recovery-state.mjs upload restore-checkpoint');
  const schedulePause = restore.indexOf('nft-vpc-restore.mjs pause-maintenance');
  const stopApp = restore.indexOf('nft-vpc-restore.mjs stop');
  assert.ok(scheduleSnapshot >= 0 && scheduleSnapshot < scheduleCheckpoint && scheduleCheckpoint < schedulePause && schedulePause < stopApp);
  const reset = workflow.split('  reset-database:')[1].split('\n  restore-app-after-database-reset:')[0];
  assert.match(reset, /needs: \[changes, restore-dataset, stop-writers-for-reset\]/);
  assert.match(reset, /needs\.restore-dataset\.result == 'success'/);
  const scheduleRecovery = workflow.split('  restore-maintenance-schedules:')[1].split('\n  maintenance-recovery-required:')[0];
  assert.ok(scheduleRecovery.indexOf('restore-app-after-database-reset.result == \'success\'') < scheduleRecovery.indexOf('nft-vpc-restore.mjs restore-maintenance'));
  assert.ok(scheduleRecovery.indexOf('restore-async-after-database-reset.result == \'success\'') < scheduleRecovery.indexOf('nft-vpc-restore.mjs restore-maintenance'));
  const compile = workflow.split('  compile-gatling:')[1].split('\n  restore-dataset:')[0];
  assert.match(compile, /name: gatling-classes-\$\{\{ github\.run_id \}\}\s+overwrite: true/);
  assert.doesNotMatch(compile, /github\.run_attempt/);
  for (const job of ['restore-dataset', 'performance-tests']) {
    const body = workflow.split(`  ${job}:`)[1].split(/\n  [a-z][a-z-]+:/)[0];
    assert.match(body, /nft-recovery-state\.mjs require-fresh-attempt/);
  }
  assert.match(workflow, /nft-recovery-state\.mjs upload load-checkpoint/);
  for (const job of ['performance-tests', 'stop-writers-for-reset', 'restore-app-after-database-reset', 'restore-async-after-database-reset']) {
    const body = workflow.split(`  ${job}:`)[1].split(/\n  [a-z][a-z-]+:/)[0];
    const credentials = body.indexOf('configure-aws-credentials');
    const download = body.indexOf('nft-recovery-state.mjs download');
    const select = body.indexOf('nft-recovery-state.mjs select');
    assert.ok(credentials >= 0 && credentials < download && download < select);
    assert.doesNotMatch(body, /merge-multiple: true/);
  }
  assert.match(workflow, /nft-recovery-state\.mjs upload cleanup-state/);
});

test('new restore attempts fail before state lookup or AWS while recovery remains retryable', () => {
  const script = fileURLToPath(new URL('../../../scripts/performance-tests/nft-vpc-restore.mjs', import.meta.url));
  const result = spawnSync(process.execPath, [script, 'stop'], {
    encoding: 'utf8',
    env: {PATH: process.env.PATH, GITHUB_RUN_ID: '123', GITHUB_RUN_ATTEMPT: '2'},
  });
  assert.equal(result.status, 1);
  assert.match(result.stdout, /fresh workflow run/i);
});

test('CLI reports controlled validation failures without exposing unexpected filesystem errors', async () => {
  const script = fileURLToPath(new URL('../../../scripts/performance-tests/nft-vpc-restore.mjs', import.meta.url));
  const validation = spawnSync(process.execPath, [script, 'stop'], {
    encoding: 'utf8',
    env: {PATH: process.env.PATH, GITHUB_RUN_ID: '123', GITHUB_RUN_ATTEMPT: '1'},
  });
  assert.equal(validation.status, 1);
  assert.match(validation.stdout, /NFT_RESTORE_STATE_PATH is required/);

  const root = await mkdtemp(join(tmpdir(), 'restore-cli-'));
  try {
    const bin = join(root, 'bin');
    await mkdir(bin);
    await writeFile(join(bin, 'aws'), `#!/usr/bin/env node
const response = process.argv[2] === 'sts'
  ? {Account: '448120078528'}
  : process.argv[2] === 's3api'
    ? {VersionId: '${certificateVersion}', ContentLength: 42}
    : {taskDefinition: ${JSON.stringify(managedRestoreDefinition(3))}};
console.log(JSON.stringify(response));
`, {mode: 0o755});
    const unexpected = spawnSync(process.execPath, [script, 'resolve-task-definition'], {
      encoding: 'utf8',
      env: {
        PATH: `${bin}:${process.env.PATH}`,
        GITHUB_RUN_ID: '123',
        GITHUB_RUN_ATTEMPT: '1',
        NFT_RESTORE_STATE_PATH: join(root, 'state.json'),
        NFT_RESTORE_TASK_DEFINITION: workerArn,
        GITHUB_OUTPUT: join(root, 'private-payload-canary', 'output'),
      },
    });
    assert.equal(unexpected.status, 1);
    assert.match(unexpected.stderr, /manual|recovery/i);
    assert.doesNotMatch(unexpected.stdout + unexpected.stderr, /private-payload-canary/);
  } finally {
    await rm(root, {recursive: true, force: true});
  }
});

test('cleanup rejects a non-boolean restoration flag as proof of completed recovery', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  deps.state.environmentRestored = 'true';
  await assert.rejects(cleanupNftEnvironment(config, async () => {
    throw new Error('Unreadable readiness proof');
  }, deps), /Unreadable readiness proof/);
});

test('cleanup retries after environment restoration without restarting already stopped writers', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  deps.state.environmentRestored = true;
  const original = deps.aws;
  deps.aws = async (service, operation, input) =>
    operation === 'list-task-definitions' ? {taskDefinitionArns: [appArn]} : original(service, operation, input);
  const readiness = async () => { throw new Error('Already restored; do not replay preparation'); };
  await cleanupNftEnvironment(config, readiness, deps);
  await cleanupNftEnvironment(config, readiness, deps);
  assert.equal(deps.state.writersStopped, true);
});

async function checkpoint(directory, name, state, readiness) {
  const path = join(directory, name);
  await mkdir(path, {recursive: true});
  await writeFile(join(path, 'nft-restore-state.json'), typeof state === 'string' ? state : JSON.stringify(state));
  if (readiness !== undefined) await writeFile(join(path, 'nft-state.json'), JSON.stringify(readiness));
}

test('recovery selects the latest same-run checkpoint and preserves its original owner', async t => {
  const recovery = await import('../../../scripts/performance-tests/nft-recovery-state.mjs');
  const directory = await mkdtemp(join(tmpdir(), 'nft-recovery-'));
  t.after(() => rm(directory, {recursive: true, force: true}));
  const state = {runId: '123-1', taskDefinition: workerArn, readinessAttempted: true};
  await checkpoint(directory, 'nft-restore-state-123-1', {...state, revision: 1});
  await checkpoint(directory, 'nft-cleanup-state-123-2', {...state, environmentRestored: true, revision: 2},
    {marker: 'PDJB-430:123-1'});
  const outputDirectory = join(directory, 'selected');
  await recovery.selectRecoveryState(directory, {runId: '123', attempt: 3, outputDirectory});
  const selected = JSON.parse(await readFile(join(outputDirectory, 'nft-restore-state.json'), 'utf8'));
  assert.equal(selected.revision, 2);
  assert.equal(recovery.recoveryOwner(selected, {runId: '123', attempt: 3}), '123-1');
  assert.deepEqual(JSON.parse(await readFile(join(outputDirectory, 'nft-state.json'), 'utf8')),
    {marker: 'PDJB-430:123-1'});
});

test('preparation checkpoint requires readiness evidence and accepts explicit no-change evidence', async t => {
  const recovery = await import('../../../scripts/performance-tests/nft-recovery-state.mjs');
  const directory = await mkdtemp(join(tmpdir(), 'nft-recovery-'));
  t.after(() => rm(directory, {recursive: true, force: true}));
  const name = 'nft-load-checkpoint-123-1';
  await checkpoint(directory, name, {runId: '123-1'});
  const path = join(directory, name, 'nft-restore-state.json');
  await recovery.markReadinessAttempted(path, {GITHUB_RUN_ID: '123', GITHUB_RUN_ATTEMPT: '1'});
  assert.equal(JSON.parse(await readFile(path, 'utf8')).readinessAttempted, true);
  const options = {runId: '123', attempt: 2, outputDirectory: join(directory, 'selected')};
  await assert.rejects(recovery.selectRecoveryState(directory, options), /readiness checkpoint/);
  const readiness = join(directory, 'nft-readiness-state-123-1');
  await mkdir(readiness);
  await writeFile(join(readiness, 'nft-state.json'), 'null');
  await recovery.selectRecoveryState(directory, options);
  assert.equal(JSON.parse(await readFile(join(options.outputDirectory, 'nft-state.json'), 'utf8')), null);
});

test('recovery rejects unsafe latest checkpoints rather than falling back to older state', async t => {
  const recovery = await import('../../../scripts/performance-tests/nft-recovery-state.mjs');
  for (const [description, artifact, state, readiness] of [
    ['wrong run', 'nft-cleanup-state-123-2', {runId: '999-1'}],
    ['future owner', 'nft-cleanup-state-123-2', {runId: '123-4'}],
    ['future checkpoint', 'nft-cleanup-state-123-4', {runId: '123-1'}],
    ['malformed newest JSON', 'nft-cleanup-state-123-2', '{'],
    ['missing readiness', 'nft-load-checkpoint-123-1', {runId: '123-1', readinessAttempted: true}],
    ['wrong readiness owner', 'nft-cleanup-state-123-2',
      {runId: '123-1', readinessAttempted: true}, {marker: 'PDJB-430:999-1'}],
  ]) {
    await t.test(description, async child => {
      const directory = await mkdtemp(join(tmpdir(), 'nft-recovery-'));
      child.after(() => rm(directory, {recursive: true, force: true}));
      await checkpoint(directory, 'nft-restore-state-123-1', {runId: '123-1'});
      await checkpoint(directory, artifact, state, readiness);
      await assert.rejects(
        recovery.selectRecoveryState(directory, {runId: '123', attempt: 3, outputDirectory: join(directory, 'selected')}),
      );
    });
  }
});

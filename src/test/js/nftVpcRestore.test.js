import assert from 'node:assert/strict';
import {test} from 'node:test';
import {restoreInVpc, startNftApp, stopNftWriters, stopRestoreTask} from '../../../scripts/performance-tests/nft-vpc-restore.mjs';
import {restoreEnvironment} from '../../../scripts/performance-tests/nft-restore.mjs';

const prefix = 'arn:aws:ecs:eu-west-2:448120078528:';
const appArn = `${prefix}task-definition/prsdb-webapp-nft:95`;
const workerArn = `${prefix}task-definition/prsdb-nft-vpc-restore-20261105:3`;
const taskArn = `${prefix}task/nft-app/worker`;
const config = {taskDefinition: workerArn, runId: '123-1'};
const app = () => ({
  serviceName: 'nft-app', status: 'ACTIVE', taskDefinition: appArn,
  desiredCount: 1, runningCount: 1, pendingCount: 0,
  deployments: [{taskDefinition: appArn, rolloutState: 'COMPLETED'}],
  networkConfiguration: {awsvpcConfiguration: {subnets: ['subnet-private'], securityGroups: ['sg-app'], assignPublicIp: 'DISABLED'}},
});
const simulator = {serviceName: 'nft-one-login-simulator', status: 'ACTIVE', desiredCount: 0, runningCount: 0, pendingCount: 0};

function dependencies(overrides = {}) {
  const calls = [];
  const state = {};
  let stopping = false;
  let polls = 0;
  return {
    calls, state,
    persist: async () => calls.push(['persist', structuredClone(state)]),
    sleep: async () => {},
    log: () => {},
    aws: async (service, operation, input = {}) => {
      calls.push([operation, input]);
      if (operation === 'get-caller-identity') return {Account: '448120078528'};
      if (operation === 'describe-services') {
        return {services: [{...app(), ...(stopping ? {desiredCount: 0, runningCount: 0} : {})}, simulator]};
      }
      if (operation === 'update-service') { stopping = true; return {}; }
      if (operation === 'list-tasks') {
        return {taskArns: input.cluster === 'nft-scheduled-tasks' ? [] : ['app-task']};
      }
      if (operation === 'describe-task-definition') {
        return {taskDefinition: {
          taskDefinitionArn: workerArn, family: 'prsdb-nft-vpc-restore-20261105', status: 'ACTIVE',
          containerDefinitions: [{name: 'nft-vpc-restore', essential: true}],
        }};
      }
      if (operation === 'run-task') return {tasks: [{taskArn}]};
      if (operation === 'describe-tasks') {
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

test('waits for actual app tasks STOPPED even after ECS reports zero running', async () => {
  const deps = dependencies();
  await stopNftWriters(config, deps);
  const polls = deps.calls.filter(([operation]) => operation === 'describe-tasks');
  assert.equal(polls.length, 2);
  assert.ok(deps.calls.find(([operation]) => operation === 'persist')[1].originalApp);
  assert.equal(deps.state.writersStopped, true);
});

test('rejects scheduled writers before stopping the app', async () => {
  const deps = dependencies();
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'list-tasks' && input.cluster === 'nft-scheduled-tasks'
    ? {taskArns: ['scheduled-writer']} : original(service, operation, input);
  await assert.rejects(stopNftWriters(config, deps), /Scheduled NFT writers/);
  assert.equal(deps.calls.some(([operation]) => operation === 'update-service'), false);
});

test('rejects a workflow SHA that is not deployed before app shutdown', async () => {
  const deps = dependencies();
  const original = deps.aws;
  deps.aws = async (service, operation, input) => operation === 'describe-task-definition'
    ? {taskDefinition: {containerDefinitions: [{name: 'prsdb-webapp', image: `nft-webapp:v0.1.0-${'a'.repeat(40)}`}]}}
    : original(service, operation, input);
  await assert.rejects(stopNftWriters({...config, expectedSha: 'b'.repeat(40)}, deps), /Workflow SHA is not deployed/);
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

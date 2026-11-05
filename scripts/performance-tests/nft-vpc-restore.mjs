import {readFile, rename, writeFile} from 'node:fs/promises';
import {randomUUID} from 'node:crypto';
import {pathToFileURL} from 'node:url';
import {join} from 'node:path';
import {
  account, appName, awsCommand, cluster, region, requireCondition, servicesFactory, stable, taskPrefix, waitFactory,
} from './nft-shared.mjs';
import {restoreEnvironment} from './nft-restore.mjs';

function validate(config) {
  requireCondition(
    new RegExp(`^${taskPrefix}prsdb-nft-vpc-restore-[a-zA-Z0-9-]+:\\d+$`).test(config.taskDefinition ?? ''),
    'An exact NFT restore task definition revision is required',
  );
  requireCondition(/^\d+-\d+$/.test(config.runId ?? ''), 'Expected GitHub run ID and attempt');
}

function runtime(overrides) {
  return {
    aws: awsCommand, state: {}, persist: async () => {}, log: console.log,
    sleep: ms => new Promise(resolve => setTimeout(resolve, ms)), now: Date.now,
    timeoutMs: 1800000, pollMs: 10000,
    ...overrides,
  };
}

async function identity(aws) {
  requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');
}

async function noScheduledWriters(aws) {
  const tasks = await aws('ecs', 'list-tasks', {cluster: 'nft-scheduled-tasks'});
  requireCondition(!tasks.nextToken && tasks.taskArns?.length === 0, 'Scheduled NFT writers are running');
}

async function describeTasks(aws, arns) {
  const result = await aws('ecs', 'describe-tasks', {cluster, tasks: arns});
  requireCondition(!result.failures?.length && result.tasks?.length === arns.length, 'NFT task lookup failed');
  return result.tasks;
}

export async function stopNftWriters(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, persist, log} = deps;
  await identity(aws);
  await noScheduledWriters(aws);
  const {app, simulator} = await servicesFactory(aws)();
  requireCondition(simulator.desiredCount === 0 && simulator.runningCount === 0 && simulator.pendingCount === 0,
    'NFT simulator must be stopped');
  if (!state.originalApp) {
    requireCondition(app.taskDefinition.startsWith(`${taskPrefix}prsdb-webapp-nft:`) &&
      stable(app, app.taskDefinition, 1), 'NFT app baseline is not stable at one task');
    if (config.expectedSha !== undefined) {
      requireCondition(/^[a-f0-9]{40}$/.test(config.expectedSha), 'Expected a full deployed commit SHA');
      const {taskDefinition} = await aws('ecs', 'describe-task-definition', {taskDefinition: app.taskDefinition});
      requireCondition(taskDefinition.containerDefinitions.some(container =>
        container.name === 'prsdb-webapp' && container.image.endsWith(`-${config.expectedSha}`)),
      'Workflow SHA is not deployed; refusing to replace the database');
    }
    state.originalApp = {taskDefinition: app.taskDefinition, desiredCount: 1};
    state.network = app.networkConfiguration.awsvpcConfiguration;
    state.taskDefinition = config.taskDefinition;
    state.runId = config.runId;
  }
  requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition,
    'Restore state belongs to another run or task definition');
  requireCondition(app.taskDefinition === state.originalApp.taskDefinition, 'NFT app revision changed');
  requireCondition([0, 1].includes(app.desiredCount), 'NFT app count changed externally');
  const arns = new Set();
  for (const desiredStatus of ['RUNNING', 'STOPPED']) {
    const listed = await aws('ecs', 'list-tasks', {cluster, serviceName: appName, desiredStatus});
    requireCondition(!listed.nextToken, 'Too many app tasks to verify safely');
    listed.taskArns.forEach(arn => arns.add(arn));
  }
  requireCondition(arns.size > 0 && arns.size <= 100, 'Cannot identify NFT application tasks');
  state.appTasks = [...arns];
  state.stopAttempted = true;
  await persist();
  await aws('ecs', 'update-service', {cluster, service: appName, desiredCount: 0});
  await waitFactory(deps)('Actual app task shutdown', async () => {
    const tasks = await describeTasks(aws, state.appTasks);
    const current = (await servicesFactory(aws)()).app;
    requireCondition(current.taskDefinition === state.originalApp.taskDefinition, 'App changed during shutdown');
    return current.desiredCount === 0 && current.runningCount === 0 && current.pendingCount === 0 &&
      tasks.every(task => task.lastStatus === 'STOPPED');
  });
  await noScheduledWriters(aws);
  state.writersStopped = true;
  await persist();
  log('App tasks are STOPPED, not merely draining; no scheduled writers are running.');
  return state;
}

export async function stopRestoreTask(overrides = {}) {
  const deps = runtime(overrides);
  const {aws, state, persist} = deps;
  if (!state.launchAttempted) return;
  if (!state.taskArn) {
    const listed = await aws('ecs', 'list-tasks', {cluster, startedBy: state.startedBy});
    requireCondition(!listed.nextToken && listed.taskArns?.length <= 1, 'Ambiguous restore launch recovery');
    requireCondition(listed.taskArns.length === 1,
      'Cannot prove whether the restore task started; manual reconciliation required before reset');
    const [task] = await describeTasks(aws, listed.taskArns);
    requireCondition(task.taskDefinitionArn === state.taskDefinition && task.startedBy === state.startedBy,
      'Recovered task does not belong to this restore');
    state.taskArn = task.taskArn;
    await persist();
  }
  requireCondition(state.taskArn.startsWith(`arn:aws:ecs:${region}:${account}:task/${cluster}/`),
    'Unexpected restore task ARN');
  const [task] = await describeTasks(aws, [state.taskArn]);
  requireCondition(task.taskDefinitionArn === state.taskDefinition, 'Restore task definition changed');
  if (task.lastStatus !== 'STOPPED') {
    await aws('ecs', 'stop-task', {cluster, task: state.taskArn, reason: 'GitHub NFT restore cleanup'});
    await waitFactory({...deps, timeoutMs: 300000})('Restore task shutdown', async () =>
      (await describeTasks(aws, [state.taskArn]))[0].lastStatus === 'STOPPED', true);
  }
}

async function workerLogs(deps) {
  const {aws, state, persist, log} = deps;
  const definition = state.worker;
  if (!definition?.logGroup) return;
  const stream = `${definition.streamPrefix}/${definition.name}/${state.taskArn.split('/').at(-1)}`;
  const streams = await aws('logs', 'describe-log-streams', {
    logGroupName: definition.logGroup, logStreamNamePrefix: stream,
  });
  if (!streams.logStreams?.some(value => value.logStreamName === stream)) return;
  const response = await aws('logs', 'get-log-events', {
    logGroupName: definition.logGroup, logStreamName: stream, startFromHead: true,
  });
  const events = response.events ?? [];
  for (const event of events.slice(state.logCount ?? 0)) {
    // Only the worker's fixed stage messages are safe; never echo PostgreSQL/AWS error output.
    if (/^\d{4}-\d\d-\d\dT[\d:]+Z (?:Installing |NFT account |Pinned dump |Dump download|Private PostgreSQL |PostgreSQL16 |PRECHECK |Rechecking |App and simulator |No scheduled |Other PostgreSQL |DESTRUCTIVE RESTORE |Restore heartbeat:|RESTORE )/.test(event.message)) {
      log(event.message);
    }
    if (event.message.includes('RESTORE TASK PASSED;')) state.workerPassed = true;
  }
  state.logCount = events.length;
  await persist();
}

export async function restoreInVpc(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, persist, log, signal} = deps;
  await identity(aws);
  requireCondition(state.writersStopped && state.taskDefinition === config.taskDefinition &&
    state.runId === config.runId, 'App shutdown must complete before VPC restore');
  const {app, simulator} = await servicesFactory(aws)();
  requireCondition(app.taskDefinition === state.originalApp.taskDefinition && app.desiredCount === 0 &&
    app.runningCount === 0 && app.pendingCount === 0 && simulator.runningCount === 0 &&
    simulator.desiredCount === 0, 'NFT writers must remain stopped');
  requireCondition((await describeTasks(aws, state.appTasks)).every(task => task.lastStatus === 'STOPPED'),
    'App containers are still running');
  await noScheduledWriters(aws);
  const {taskDefinition} = await aws('ecs', 'describe-task-definition', {taskDefinition: config.taskDefinition});
  requireCondition(taskDefinition?.taskDefinitionArn === config.taskDefinition && taskDefinition.status === 'ACTIVE',
    'Restore task revision is unavailable');
  const essential = taskDefinition.containerDefinitions.filter(container => container.essential !== false);
  requireCondition(taskDefinition.containerDefinitions.length === 1 &&
    essential.length === 1 && essential[0].name === 'nft-vpc-restore',
    'Expected the dedicated NFT restore essential container');
  const worker = essential[0];
  state.worker = {
    name: worker.name, logGroup: worker.logConfiguration?.options?.['awslogs-group'],
    streamPrefix: worker.logConfiguration?.options?.['awslogs-stream-prefix'],
  };
  requireCondition(!state.launchAttempted, 'Restore already launched; use cleanup, not another restore');
  state.startedBy = `nft-restore-${config.runId}`;
  state.clientToken = randomUUID();
  state.launchAttempted = true;
  await persist();
  const launched = await aws('ecs', 'run-task', {
    cluster, taskDefinition: config.taskDefinition, count: 1, launchType: 'FARGATE',
    startedBy: state.startedBy, clientToken: state.clientToken,
    networkConfiguration: {awsvpcConfiguration: {...state.network, assignPublicIp: 'DISABLED'}},
  });
  if (launched.tasks?.length === 1) {
    state.taskArn = launched.tasks[0].taskArn;
    await persist();
  }
  requireCondition(!launched.failures?.length && state.taskArn, 'NFT restore task launch failed');
  log(`Restore task launched: ${state.taskArn}`);
  try {
    await waitFactory(deps)('In-VPC restore', async () => {
      const [task] = await describeTasks(aws, [state.taskArn]);
      requireCondition(task.taskDefinitionArn === config.taskDefinition, 'Unexpected restore task definition');
      await workerLogs(deps);
      if (task.lastStatus !== 'STOPPED') return false;
      state.containers = task.containers.map(value => ({name: value.name, exitCode: value.exitCode}));
      await persist();
      requireCondition(essential.every(container =>
        task.containers.some(value => value.name === container.name && value.exitCode === 0)),
      'NFT restore essential container failed or has no exit code');
      return true;
    }, false, signal);
    state.restorePassed = true;
    await persist();
    log('In-VPC restore succeeded; app stays stopped for migrations and validation.');
  } finally {
    await stopRestoreTask(deps);
  }
}

async function healthyAppTarget(aws, app) {
  const listed = await aws('ecs', 'list-tasks', {cluster, serviceName: appName});
  if (listed.nextToken || listed.taskArns?.length !== 1) return false;
  const [task] = await describeTasks(aws, listed.taskArns);
  const ips = task.containers.flatMap(container => (container.networkInterfaces ?? []).map(value => value.privateIpv4Address));
  const health = await aws('elbv2', 'describe-target-health', {TargetGroupArn: app.loadBalancers[0].targetGroupArn});
  return health.TargetHealthDescriptions?.length === 1 &&
    health.TargetHealthDescriptions.every(value => ips.includes(value.Target.Id) && value.TargetHealth.State === 'healthy');
}

export async function startNftApp(overrides = {}, {afterReset = false} = {}) {
  const deps = runtime(overrides);
  const {aws, state} = deps;
  await identity(aws);
  requireCondition(state.originalApp && (state.restorePassed || afterReset), 'Successful database operation required');
  const {app} = await servicesFactory(aws)();
  requireCondition(app.taskDefinition === state.originalApp.taskDefinition, 'Refusing to start a different app revision');
  await aws('ecs', 'update-service', {cluster, service: appName, desiredCount: 1});
  await waitFactory(deps)('App rollout and ALB health', async () => {
    const current = (await servicesFactory(aws)()).app;
    requireCondition(current.taskDefinition === state.originalApp.taskDefinition, 'App changed during startup');
    if (!stable(current, state.originalApp.taskDefinition, 1)) return false;
    return healthyAppTarget(aws, current);
  });
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const controller = new AbortController();
  const abort = () => controller.abort(new Error('NFT restore interrupted'));
  process.on('SIGTERM', abort);
  process.on('SIGINT', abort);
  try {
    const command = process.argv[2];
    const path = process.env.NFT_RESTORE_STATE_PATH ||
      (process.env.RUNNER_TEMP && join(process.env.RUNNER_TEMP, 'nft-restore-state.json'));
    requireCondition(path, 'NFT_RESTORE_STATE_PATH is required');
    const config = {
      taskDefinition: process.env.NFT_RESTORE_TASK_DEFINITION,
      runId: `${process.env.GITHUB_RUN_ID}-${process.env.GITHUB_RUN_ATTEMPT}`,
      expectedSha: process.env.EXPECTED_NFT_SHA,
    };
    const state = await readFile(path, 'utf8').then(JSON.parse).catch(error => {
      if (command === 'stop' && error.code === 'ENOENT') return {};
      throw new Error('NFT restore state is missing or unreadable; manual recovery required');
    });
    const deps = runtime({
      state, signal: controller.signal,
      persist: async () => {
        const temporary = `${path}.tmp`;
        await writeFile(temporary, JSON.stringify(state));
        await rename(temporary, path);
      },
    });
    if (command === 'stop') {
      requireCondition(config.expectedSha, 'EXPECTED_NFT_SHA is required before stopping the app');
      await stopNftWriters(config, deps);
    }
    else if (command === 'run') await restoreInVpc(config, deps);
    else if (command === 'start') await startNftApp(deps);
    else if (command === 'start-after-reset') await startNftApp(deps, {afterReset: true});
    else if (command === 'cleanup') {
      await identity(deps.aws);
      requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition,
        'Cleanup state belongs to another workflow');
      await stopRestoreTask(deps);
      const readinessPath = process.env.READINESS_STATE_PATH ||
        (process.env.RUNNER_TEMP && join(process.env.RUNNER_TEMP, 'nft-state.json'));
      requireCondition(readinessPath, 'READINESS_STATE_PATH is required outside GitHub Actions');
      const readiness = await readFile(readinessPath, 'utf8').then(JSON.parse).catch(error => {
        if (error.code === 'ENOENT') return null;
        throw new Error('Readiness state cannot be read safely');
      });
      await restoreEnvironment(readiness, {
        aws: deps.aws, timeoutMs: 1200000, appHealthy: app => healthyAppTarget(deps.aws, app),
      });
      await stopNftWriters(config, deps);
      const latest = await deps.aws('ecs', 'list-task-definitions', {
        familyPrefix: 'prsdb-webapp-nft', status: 'ACTIVE', sort: 'DESC', maxResults: 1,
      });
      requireCondition(latest.taskDefinitionArns?.[0] === state.originalApp.taskDefinition,
        'Latest app revision is not the reset baseline; refusing automatic reset');
    } else throw new Error('Expected stop, run, start, start-after-reset or cleanup');
  } catch (error) {
    console.log(`NFT VPC restore details: ${error.message}`);
    console.error('NFT VPC operation failed; keep the app stopped until recovery succeeds.');
    process.exitCode = 1;
  } finally {
    process.off('SIGTERM', abort);
    process.off('SIGINT', abort);
  }
}

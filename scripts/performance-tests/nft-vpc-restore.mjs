import {readFile, rename, writeFile} from 'node:fs/promises';
import {randomUUID} from 'node:crypto';
import {pathToFileURL} from 'node:url';
import {dirname, join} from 'node:path';
import {isDeepStrictEqual} from 'node:util';
import {
  account, appName, awsCommand, cluster, region, servicesFactory, stable, taskPrefix, waitFactory,
} from './nft-shared.mjs';
import {cloneForScanWorker, scanQueueArn, scanQueueName, scanQueueUrl} from './nft-scan-worker.mjs';
import {restoreEnvironment} from './nft-restore.mjs';
import {recoveryOwner, requireFreshAttempt, SafeRecoveryError, uploadRecoveryCheckpoints} from './nft-recovery-state.mjs';

function requireCondition(condition, message) {
  if (!condition) throw new SafeRecoveryError(message);
}

const asyncName = 'nft-async-worker';
const restoreTaskFamily = 'prsdb-nft-vpc-restore-performance';
const restoreContainerName = 'nft-vpc-restore';
const certificateBucket = 'prsdb-uploaded-files-nft';
const maintenanceGroup = 'nft-scheduled-tasks-group';
const scheduledCluster = 'nft-scheduled-tasks';
const scheduledClusterArn = `arn:aws:ecs:${region}:${account}:cluster/${scheduledCluster}`;
const scheduledRoleArn = `arn:aws:iam::${account}:role/nft-scheduled-tasks`;
const maintenanceSchedules = new Map([
  ['nft-jl-invitation-expiry-email-scheduled-task', 'prsdb-jl-invitation-expiry-email-scheduled-task-nft'],
  ['nft-reconcile-payments-scheduled-task', 'prsdb-reconcile-payments-scheduled-task-nft'],
  ['nft-incomplete-property-reminder-scheduled-task', 'prsdb-incomplete-property-reminder-scheduled-task-nft'],
  ['nft-delete-incomplete-properties-scheduled-task', 'prsdb-delete-incomplete-properties-scheduled-task-nft'],
  ['nft-org-ll-invitation-deletion-scheduled-task', 'prsdb-org-ll-invitation-deletion-scheduled-task-nft'],
  ['nft-jl-invitation-deletion-scheduled-task', 'prsdb-jl-invitation-deletion-scheduled-task-nft'],
  ['nft-ngd-address-update-scheduled-task', 'prsdb-ngd-address-update-scheduled-task-nft'],
]);
const scheduleUpdateFields = [
  'ActionAfterCompletion', 'Description', 'EndDate', 'FlexibleTimeWindow', 'GroupName', 'KmsKeyArn', 'Name',
  'ScheduleExpression', 'ScheduleExpressionTimezone', 'StartDate', 'State', 'Target',
];

function validate(config) {
  const definition = config.taskDefinition ?? '';
  requireCondition(
    definition.startsWith(taskPrefix) && /^prsdb-nft-vpc-restore-[a-zA-Z0-9-]+:\d+$/.test(definition.slice(taskPrefix.length)),
    'An exact NFT restore task definition revision is required',
  );
  requireCondition(/^\d+-\d+$/.test(config.runId ?? ''), 'Expected GitHub run ID and attempt');
}

function immutableVersion(value) {
  return typeof value === 'string' && value.length > 0 && value === value.trim() &&
    !/^(?:null|none|undefined)$/i.test(value) && !/[\x00-\x1f\x7f]/.test(value);
}

function restoreDefinitionIdentity(taskDefinition, taskDefinitionArn) {
  requireCondition(taskDefinition?.taskDefinitionArn === taskDefinitionArn &&
    taskDefinition.family === restoreTaskFamily && taskDefinition.status === 'ACTIVE' &&
    taskDefinition.containerDefinitions?.length === 1,
  'Expected the active managed NFT restore task definition');
  const [container] = taskDefinition.containerDefinitions;
  requireCondition(container.name === restoreContainerName && container.essential !== false,
    'Expected the dedicated NFT restore essential container');
  const entries = container.environment ?? [];
  const names = entries.map(({name}) => name);
  requireCondition(new Set(names).size === names.length, 'Managed NFT restore task environment has duplicate names');
  const environment = Object.fromEntries(entries.map(({name, value}) => [name, value]));
  const dumpVersion = environment.NFT_DUMP_VERSION;
  const dumpBytesValue = environment.NFT_DUMP_BYTES;
  const dumpBytes = Number(dumpBytesValue);
  const dumpSha256 = environment.NFT_DUMP_SHA256;
  requireCondition(immutableVersion(dumpVersion) &&
    /^\d+$/.test(dumpBytesValue ?? '') && Number.isSafeInteger(dumpBytes) && dumpBytes > 0 &&
    /^[a-f0-9]{64}$/.test(dumpSha256 ?? ''),
  'Managed NFT restore task dump identity is invalid');
  const certificateKey = environment.NFT_CERTIFICATE_KEY;
  const certificateVersion = environment.NFT_CERTIFICATE_VERSION;
  requireCondition(typeof certificateKey === 'string' &&
    (certificateKey === 'seeding/certificate_template.png' ||
      /^seeding\/certificate-references\/[1-9][0-9]*-[1-9][0-9]*\.png$/.test(certificateKey)) &&
    immutableVersion(certificateVersion) &&
    environment.NFT_CERTIFICATE_DUMP_VERSION === dumpVersion &&
    environment.NFT_CERTIFICATE_DUMP_BYTES === dumpBytesValue &&
    environment.NFT_CERTIFICATE_DUMP_SHA256 === dumpSha256,
  'Managed NFT restore certificate identity or dump binding is invalid; publish matching immutable seed metadata');
  return {taskDefinitionArn, dumpVersion, dumpBytes, dumpSha256, certificateKey, certificateVersion};
}

async function checkCertificate(aws, definition) {
  let object;
  try {
    object = await aws('s3api', 'head-object', {
      Bucket: certificateBucket, Key: definition.certificateKey, VersionId: definition.certificateVersion,
    });
  } catch {
    throw new SafeRecoveryError('Pinned NFT certificate version is unavailable or inaccessible; verify access or regenerate the seed and publish matching metadata');
  }
  requireCondition(object?.VersionId === definition.certificateVersion &&
    Number.isSafeInteger(object.ContentLength) && object.ContentLength > 0 &&
    (object.DeleteMarker === undefined || object.DeleteMarker === false),
  'Pinned NFT certificate version is missing, empty, or invalid; verify the seed or regenerate it and publish matching metadata');
}

function requireCertificateCheckpoint(state, config) {
  const definition = state.restoreDefinition;
  requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition &&
    definition?.taskDefinitionArn === config.taskDefinition &&
    typeof definition.certificateKey === 'string' &&
    (definition.certificateKey === 'seeding/certificate_template.png' ||
      /^seeding\/certificate-references\/[1-9][0-9]*-[1-9][0-9]*\.png$/.test(definition.certificateKey)) &&
    immutableVersion(definition.certificateVersion) && immutableVersion(definition.dumpVersion) &&
    Number.isSafeInteger(definition.dumpBytes) && definition.dumpBytes > 0 &&
    /^[a-f0-9]{64}$/.test(definition.dumpSha256 ?? ''),
  'NFT certificate preflight checkpoint is missing or invalid; resolve the pinned restore task before mutation');
}

export async function resolveRestoreTaskDefinition(config, overrides = {}) {
  requireCondition(/^\d+-\d+$/.test(config.runId ?? ''), 'Expected GitHub run ID and attempt');
  const deps = runtime(overrides);
  const {aws, state, persist} = deps;
  await identity(aws);
  requireCondition(state.runId === undefined || state.runId === config.runId,
    'NFT recovery checkpoint belongs to another workflow');
  let taskDefinitionArn = config.taskDefinition ?? state.taskDefinition;
  if (!taskDefinitionArn) {
    const listed = await aws('ecs', 'list-task-definitions', {
      familyPrefix: restoreTaskFamily, status: 'ACTIVE', sort: 'DESC', maxResults: 100,
    });
    const matching = listed.taskDefinitionArns?.filter(arn =>
      new RegExp(`^${taskPrefix}${restoreTaskFamily}:\\d+$`).test(arn));
    requireCondition(Array.isArray(matching) && matching.length > 0,
      'Active managed NFT restore task revision could not be resolved');
    taskDefinitionArn = matching.sort((left, right) =>
      Number(right.slice(right.lastIndexOf(':') + 1)) - Number(left.slice(left.lastIndexOf(':') + 1)))[0];
  }
  requireCondition(
    new RegExp(`^${taskPrefix}${restoreTaskFamily}:\\d+$`).test(taskDefinitionArn),
    'Restore task override is not an exact managed NFT restore revision',
  );
  const {taskDefinition} = await aws('ecs', 'describe-task-definition', {taskDefinition: taskDefinitionArn});
  const restoreDefinition = restoreDefinitionIdentity(taskDefinition, taskDefinitionArn);
  requireCondition(state.taskDefinition === undefined || state.taskDefinition === taskDefinitionArn,
    'NFT recovery checkpoint belongs to another restore task revision');
  requireCondition(state.restoreDefinition === undefined ||
    isDeepStrictEqual(state.restoreDefinition, restoreDefinition),
  'NFT recovery checkpoint has a different immutable dump or certificate identity');
  await checkCertificate(aws, restoreDefinition);
  state.taskDefinition = taskDefinitionArn;
  state.restoreDefinition = restoreDefinition;
  state.runId = config.runId;
  state.expectedSha = config.expectedSha;
  state.resetSha = config.resetSha;
  await persist();
  return taskDefinitionArn;
}

function runtime(overrides) {
  return {
    aws: awsCommand, state: {}, persist: async () => {}, checkpoint: async () => {}, log: console.log,
    sleep: ms => new Promise(resolve => setTimeout(resolve, ms)), now: Date.now,
    timeoutMs: 1800000, pollMs: 10000,
    ...overrides,
  };
}

async function identity(aws) {
  requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');
}

function scheduleUpdate(schedule, state = schedule.State) {
  return Object.fromEntries(scheduleUpdateFields
    .filter(field => Object.hasOwn(schedule, field))
    .map(field => [field, field === 'State' ? state : schedule[field]]));
}

function validateMaintenanceSchedule(schedule, name) {
  const taskDefinition = maintenanceSchedules.get(name);
  const target = schedule?.Target;
  const ecs = target?.EcsParameters;
  const network = ecs?.NetworkConfiguration?.awsvpcConfiguration;
  requireCondition(schedule?.Name === name && schedule.GroupName === maintenanceGroup &&
    schedule.Arn === `arn:aws:scheduler:${region}:${account}:schedule/${maintenanceGroup}/${name}`,
  'Unexpected NFT maintenance schedule identity');
  requireCondition(['ENABLED', 'DISABLED'].includes(schedule.State), 'Unexpected NFT maintenance schedule state');
  requireCondition(schedule.ScheduleExpressionTimezone === 'UTC' && schedule.FlexibleTimeWindow?.Mode === 'OFF',
    'Unexpected NFT maintenance schedule configuration');
  requireCondition(target?.Arn === scheduledClusterArn && target.RoleArn === scheduledRoleArn &&
    ecs?.TaskDefinitionArn === `arn:aws:ecs:${region}:${account}:task-definition/${taskDefinition}` &&
    ecs.LaunchType === 'FARGATE' && ecs.TaskCount === 1 && network?.AssignPublicIp === 'DISABLED' &&
    Array.isArray(network.Subnets) && network.Subnets.length > 0 &&
    Array.isArray(network.SecurityGroups) && network.SecurityGroups.length > 0,
  'Unexpected NFT maintenance schedule target');
}

function scheduleConfigMatches(current, original) {
  const currentConfig = scheduleUpdate(current);
  const originalConfig = scheduleUpdate(original);
  delete currentConfig.State;
  delete originalConfig.State;
  return isDeepStrictEqual(currentConfig, originalConfig);
}

async function getMaintenanceSchedule(aws, name) {
  const schedule = await aws('scheduler', 'get-schedule', {GroupName: maintenanceGroup, Name: name});
  validateMaintenanceSchedule(schedule, name);
  return schedule;
}

async function persistMaintenanceProgress(deps) {
  await deps.persist();
  await deps.checkpoint();
}

export async function snapshotMaintenanceSchedules(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, persist} = deps;
  requireCondition(state.runId === undefined || state.runId === config.runId,
    'NFT maintenance snapshot belongs to another workflow');
  requireCondition(state.taskDefinition === undefined || state.taskDefinition === config.taskDefinition,
    'NFT maintenance snapshot uses another restore revision');
  await identity(aws);
  if (state.maintenanceScheduleSnapshot) return state;

  const listed = await aws('scheduler', 'list-schedules', {GroupName: maintenanceGroup, MaxResults: 100});
  const schedules = Array.isArray(listed.Schedules) ? listed.Schedules : [];
  const names = schedules.map(schedule => schedule.Name).sort();
  requireCondition(!listed.NextToken && !listed.nextToken && Array.isArray(listed.Schedules) &&
    schedules.length === maintenanceSchedules.size &&
    isDeepStrictEqual(names, [...maintenanceSchedules.keys()].sort()),
  'NFT maintenance schedule inventory is incomplete or unexpected');

  const snapshots = [];
  for (const name of [...maintenanceSchedules.keys()].sort()) {
    const listedSchedule = schedules.find(schedule => schedule.Name === name);
    requireCondition(listedSchedule.GroupName === maintenanceGroup &&
      listedSchedule.Arn === `arn:aws:scheduler:${region}:${account}:schedule/${maintenanceGroup}/${name}`,
    'Unexpected NFT maintenance schedule listing');
    snapshots.push(await getMaintenanceSchedule(aws, name));
  }

  state.runId = config.runId;
  state.taskDefinition = config.taskDefinition;
  state.expectedSha = config.expectedSha;
  state.resetSha = config.resetSha;
  state.maintenanceScheduleSnapshot = snapshots;
  state.maintenanceScheduleProgress = {};
  state.maintenancePaused = false;
  await persist();
  return state;
}

export async function pauseMaintenanceSchedules(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, log} = deps;
  await identity(aws);
  requireCertificateCheckpoint(state, config);
  requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition &&
    Array.isArray(state.maintenanceScheduleSnapshot) && state.maintenanceScheduleSnapshot.length === maintenanceSchedules.size,
  'NFT maintenance schedule snapshot is missing or belongs to another workflow');
  state.maintenanceScheduleProgress ??= {};

  for (const original of state.maintenanceScheduleSnapshot) {
    const name = original.Name;
    validateMaintenanceSchedule(original, name);
    const current = await getMaintenanceSchedule(aws, name);
    requireCondition(scheduleConfigMatches(current, original),
      'NFT maintenance schedule configuration changed after snapshot; refusing pause');
    const progress = state.maintenanceScheduleProgress[name] ?? {};
    requireCondition(original.State === 'DISABLED'
      ? current.State === 'DISABLED'
      : current.State === 'DISABLED'
        ? progress.pauseAttempted === true
        : current.State === 'ENABLED' && progress.paused !== true,
    'NFT maintenance schedule state changed outside this recovery');
  }

  for (const original of state.maintenanceScheduleSnapshot) {
    const name = original.Name;
    validateMaintenanceSchedule(original, name);
    const current = await getMaintenanceSchedule(aws, name);
    requireCondition(scheduleConfigMatches(current, original),
      'NFT maintenance schedule configuration changed after snapshot; refusing pause');
    const progress = state.maintenanceScheduleProgress[name] ?? {};
    if (original.State === 'DISABLED') {
      requireCondition(current.State === 'DISABLED', 'Disabled NFT maintenance schedule changed externally');
      state.maintenanceScheduleProgress[name] = {...progress, paused: true, preservedDisabled: true};
      await persistMaintenanceProgress(deps);
      continue;
    }
    requireCondition(current.State === 'DISABLED'
      ? progress.pauseAttempted === true
      : current.State === 'ENABLED' && progress.paused !== true,
    'NFT maintenance schedule state changed outside this recovery');
    if (current.State === 'ENABLED') {
      state.maintenanceScheduleProgress[name] = {...progress, pauseAttempted: true, paused: false};
      await persistMaintenanceProgress(deps);
      await aws('scheduler', 'update-schedule', scheduleUpdate(original, 'DISABLED'));
    }
    const verified = await getMaintenanceSchedule(aws, name);
    requireCondition(verified.State === 'DISABLED' && scheduleConfigMatches(verified, original),
      'NFT maintenance schedule pause could not be verified');
    state.maintenanceScheduleProgress[name] = {...state.maintenanceScheduleProgress[name], paused: true};
    await persistMaintenanceProgress(deps);
  }

  await waitFactory(deps)('Scheduled NFT writer drain', async () => {
    const tasks = await aws('ecs', 'list-tasks', {cluster: scheduledCluster});
    requireCondition(!tasks.nextToken && Array.isArray(tasks.taskArns), 'Cannot identify scheduled NFT writers');
    return tasks.taskArns.length === 0;
  }, false, deps.signal);
  state.maintenancePaused = true;
  state.maintenancePauseComplete = true;
  await persistMaintenanceProgress(deps);
  log('All seven NFT maintenance schedules are disabled or preserved disabled; scheduled writers are drained.');
  return state;
}

export async function restoreMaintenanceSchedules(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, log} = deps;
  await identity(aws);
  requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition &&
    Array.isArray(state.maintenanceScheduleSnapshot) && state.maintenanceScheduleSnapshot.length === maintenanceSchedules.size,
  'NFT maintenance schedule recovery snapshot is missing or belongs to another workflow');
  const safeAbort = config.safeAbort === true;
  if (safeAbort) {
    requireCondition(!state.originalApp && !state.stopAttempted && !state.launchAttempted && !state.restorePassed &&
      !state.asyncRegistrationAttempted && !state.asyncSwitchAttempted && !state.environmentRestored,
    'Destructive NFT work may have started; keep maintenance schedules paused');
  } else {
    requireCondition(config.recoveryVerified === true && config.resetVerified === true &&
      state.resetVerified === true && state.qaVerified === true && state.asyncRestored === true &&
      (state.maintenancePaused === true || state.maintenanceRestored === true),
    'Verified database reset, application QA, and async recovery are required before resuming maintenance');
  }
  state.maintenanceScheduleProgress ??= {};

  for (const original of state.maintenanceScheduleSnapshot) {
    const name = original.Name;
    validateMaintenanceSchedule(original, name);
    const current = await getMaintenanceSchedule(aws, name);
    requireCondition(scheduleConfigMatches(current, original),
      'NFT maintenance schedule configuration drifted; refusing recovery');
    const progress = state.maintenanceScheduleProgress[name] ?? {};
    if (original.State === 'DISABLED') {
      requireCondition(current.State === 'DISABLED', 'Originally disabled NFT schedule was changed externally');
    } else if (current.State === 'ENABLED') {
      requireCondition(progress.restoreAttempted === true || safeAbort,
        'NFT maintenance schedule was enabled outside this recovery');
    } else {
      requireCondition(current.State === 'DISABLED' && progress.pauseAttempted === true &&
        (progress.paused === true || safeAbort),
      'NFT maintenance schedule pause is not owned by this recovery');
    }
  }

  for (const original of state.maintenanceScheduleSnapshot) {
    const name = original.Name;
    validateMaintenanceSchedule(original, name);
    const current = await getMaintenanceSchedule(aws, name);
    requireCondition(scheduleConfigMatches(current, original),
      'NFT maintenance schedule configuration drifted; refusing recovery');
    const progress = state.maintenanceScheduleProgress[name] ?? {};
    if (original.State === 'DISABLED') {
      requireCondition(current.State === 'DISABLED', 'Originally disabled NFT schedule was changed externally');
      state.maintenanceScheduleProgress[name] = {...progress, restored: true};
      await persistMaintenanceProgress(deps);
      continue;
    }
    if (current.State === 'ENABLED') {
      requireCondition(progress.restoreAttempted === true || safeAbort,
        'NFT maintenance schedule was enabled outside this recovery');
      state.maintenanceScheduleProgress[name] = {...progress, restored: true};
      await persistMaintenanceProgress(deps);
      continue;
    }
    requireCondition(current.State === 'DISABLED' && progress.pauseAttempted === true &&
      (progress.paused === true || safeAbort),
      'NFT maintenance schedule pause is not owned by this recovery');
    state.maintenanceScheduleProgress[name] = {...progress, restoreAttempted: true};
    await persistMaintenanceProgress(deps);
    await aws('scheduler', 'update-schedule', scheduleUpdate(original));
    const verified = await getMaintenanceSchedule(aws, name);
    requireCondition(verified.State === original.State && isDeepStrictEqual(scheduleUpdate(verified), scheduleUpdate(original)),
      'NFT maintenance schedule restoration could not be verified');
    state.maintenanceScheduleProgress[name] = {...state.maintenanceScheduleProgress[name], restored: true};
    await persistMaintenanceProgress(deps);
  }

  state.maintenancePaused = false;
  state.maintenanceRestored = true;
  await persistMaintenanceProgress(deps);
  log('All seven NFT maintenance schedules match their exact snapshotted states.');
  return state;
}

async function noScheduledWriters(aws) {
  const tasks = await aws('ecs', 'list-tasks', {cluster: 'nft-scheduled-tasks'});
  requireCondition(!tasks.nextToken && tasks.taskArns?.length === 0, 'Scheduled NFT writers are running');
}

async function describeTasks(aws, arns) {
  if (!arns.length) return [];
  const result = await aws('ecs', 'describe-tasks', {cluster, tasks: arns});
  requireCondition(!result.failures?.length && result.tasks?.length === arns.length, 'NFT task lookup failed');
  return result.tasks;
}

async function asyncService(aws) {
  const result = await aws('ecs', 'describe-services', {cluster, services: [asyncName]});
  const service = result.services?.[0];
  requireCondition(!result.failures?.length && result.services?.length === 1 &&
    service.serviceName === asyncName && service.status === 'ACTIVE', 'NFT async service missing');
  return service;
}

async function scanQueueEmpty(aws) {
  const {Attributes: attributes} = await aws('sqs', 'get-queue-attributes', {
    QueueUrl: scanQueueUrl,
    AttributeNames: ['QueueArn', 'ApproximateNumberOfMessages', 'ApproximateNumberOfMessagesNotVisible', 'ApproximateNumberOfMessagesDelayed'],
  });
  requireCondition(attributes?.QueueArn === scanQueueArn, 'NFT scan queue identity is unverified');
  return ['ApproximateNumberOfMessages', 'ApproximateNumberOfMessagesNotVisible', 'ApproximateNumberOfMessagesDelayed']
    .every(name => attributes[name] === '0');
}

async function runningCandidateImage(aws, app, definition) {
  const image = definition.containerDefinitions.find(value => value.name === 'prsdb-webapp').image;
  const listed = await aws('ecs', 'list-tasks', {cluster, serviceName: appName});
  requireCondition(!listed.nextToken && listed.taskArns?.length === 1, 'Candidate app task identity is unverified');
  const [task] = await describeTasks(aws, listed.taskArns);
  const container = task.containers?.find(value => value.name === 'prsdb-webapp');
  requireCondition(task.taskDefinitionArn === app.taskDefinition && task.lastStatus === 'RUNNING' &&
    container?.image === image && /^sha256:[a-f0-9]{64}$/.test(container.imageDigest ?? ''),
  'Candidate app image digest is unverified');
  return `${image.replace(/:[^/]+$/, '')}@${container.imageDigest}`;
}

async function runningServiceImageDigest(aws, serviceName, service, taskDefinition, containerName) {
  if (service.desiredCount === 0) return undefined;
  const listed = await aws('ecs', 'list-tasks', {cluster, serviceName});
  requireCondition(!listed.nextToken && listed.taskArns?.length === service.desiredCount &&
    service.runningCount === service.desiredCount,
  'NFT deployed image tasks are not stable');
  const tasks = await describeTasks(aws, listed.taskArns);
  const digests = tasks.map(task => {
    const container = task.containers?.find(value => value.name === containerName);
    requireCondition(task.taskDefinitionArn === service.taskDefinition && task.lastStatus === 'RUNNING' &&
      /^sha256:[a-f0-9]{64}$/.test(container?.imageDigest ?? ''),
    'NFT deployed image digest is unverified');
    return container.imageDigest;
  });
  requireCondition(new Set(digests).size === 1, 'NFT service tasks use different image digests');
  return digests[0];
}

async function verifyResetCompatibility(aws, app, asyncWorker, appDefinition, workerDefinition, config) {
  requireCondition(appDefinition?.taskDefinitionArn === app.taskDefinition && appDefinition.status === 'ACTIVE' &&
    workerDefinition?.taskDefinitionArn === asyncWorker.taskDefinition && workerDefinition.status === 'ACTIVE',
  'NFT deployed app or worker task definition is not active');
  const appContainer = appDefinition.containerDefinitions?.find(value => value.name === 'prsdb-webapp');
  const workerContainer = workerDefinition.containerDefinitions?.find(value => value.name === 'prsdb-async-worker');
  requireCondition(appContainer?.image, 'NFT app task image is missing');
  requireCondition(/^[a-f0-9]{40}$/.test(config.expectedSha ?? '') &&
    appContainer.image.endsWith(`-${config.expectedSha}`),
  'Workflow SHA is not deployed; refusing to replace the database');
  if (asyncWorker.desiredCount === 0) {
    return {
      mode: 'worker-stopped',
      resetSha: config.resetSha,
      appSha: config.expectedSha,
      appTaskDefinition: app.taskDefinition,
      asyncTaskDefinition: asyncWorker.taskDefinition,
      appImage: appContainer.image,
    };
  }
  requireCondition(workerContainer?.image === appContainer.image,
    'NFT app and async worker task images do not match');
  const appImageDigest = await runningServiceImageDigest(aws, appName, app, appDefinition, 'prsdb-webapp');
  const workerImageDigest = await runningServiceImageDigest(aws, asyncName, asyncWorker, workerDefinition, 'prsdb-async-worker');
  requireCondition(asyncWorker.desiredCount === 0 || workerImageDigest === appImageDigest,
    'NFT app and async worker running image digests do not match');

  if (config.expectedSha === config.resetSha) {
    return {
      mode: 'matching-workflow',
      resetSha: config.resetSha,
      appSha: config.expectedSha,
      appTaskDefinition: app.taskDefinition,
      asyncTaskDefinition: asyncWorker.taskDefinition,
      appImage: appContainer.image,
      appImageDigest,
      asyncImage: workerContainer.image,
      asyncImageDigest: workerImageDigest,
    };
  }

  requireCondition(/^[a-f0-9]{40}$/.test(config.resetSha ?? '') &&
    config.resetCompatibleTaskDefinition === asyncWorker.taskDefinition &&
    config.resetCompatibleSha === config.resetSha,
  'Explicit reset compatibility pins are required when deployed app SHA differs from workflow SHA');
  return {
    mode: 'explicit-pin',
    resetSha: config.resetSha,
    appSha: config.expectedSha,
    appTaskDefinition: app.taskDefinition,
    asyncTaskDefinition: asyncWorker.taskDefinition,
    compatibilityTaskDefinition: config.resetCompatibleTaskDefinition,
    compatibilitySha: config.resetCompatibleSha,
    appImage: appContainer.image,
    appImageDigest,
    asyncImage: workerContainer.image,
    asyncImageDigest: workerImageDigest,
  };
}

async function recoverAsyncRegistration(deps, discover = false) {
  const {aws, state, persist} = deps;
  if ((!state.asyncRegistrationAttempted && !discover) || state.candidateAsync || state.asyncRetired) return;
  const family = `prsdb-async-worker-nft-performance-${state.runId}`;
  const listed = await aws('ecs', 'list-task-definitions', {familyPrefix: family, status: 'ACTIVE'});
  const matches = listed.taskDefinitionArns?.filter(arn => arn.startsWith(`${taskPrefix}${family}:`));
  if (discover && !state.asyncRegistrationAttempted && !listed.nextToken && matches?.length === 0) return;
  requireCondition(!listed.nextToken && matches?.length === 1,
    'Candidate worker registration is unresolved; operator recovery required');
  const {taskDefinition: candidate} = await aws('ecs', 'describe-task-definition', {taskDefinition: matches[0]});
  const {taskDefinition: baseline} = await aws('ecs', 'describe-task-definition', {taskDefinition: state.originalAsync.taskDefinition});
  const {taskDefinition: app} = await aws('ecs', 'describe-task-definition', {taskDefinition: state.originalApp.taskDefinition});
  const expected = cloneForScanWorker(baseline, app, {runId: state.runId, expectedSha: state.expectedSha});
  expected.containerDefinitions[0].image = state.asyncImage ??
    await runningCandidateImage(aws, (await servicesFactory(aws)()).app, app);
  requireCondition(candidate?.status === 'ACTIVE' && candidate.taskDefinitionArn === matches[0] &&
    Object.entries(expected).every(([key, value]) => isDeepStrictEqual(candidate[key], value)),
  'Candidate worker registration identity is unverified');
  state.candidateAsync = matches[0];
  state.asyncRegistrationAttempted = true;
  state.asyncImage = expected.containerDefinitions[0].image;
  // A lost checkpoint may also have lost the service-switch response.
  state.asyncSwitchAttempted = true;
  await persist();
}

async function stopAsyncWorker(deps) {
  const {aws, state, persist} = deps;
  await recoverAsyncRegistration(deps);
  const service = await asyncService(aws);
  requireCondition(state.originalAsync &&
    [state.originalAsync.taskDefinition, state.candidateAsync].includes(service.taskDefinition),
    'NFT async service revision changed');
  requireCondition([0, state.originalAsync.desiredCount, ...(state.asyncSwitchAttempted ? [1] : [])]
    .includes(service.desiredCount), 'NFT async service count changed');
  if (state.asyncSwitchAttempted) {
    await waitFactory(deps)('NFT scan queue quiescence', () => scanQueueEmpty(aws), true);
  } else requireCondition(await scanQueueEmpty(aws), 'NFT scan queue is not empty; refusing database replacement');
  const revision = service.taskDefinition;
  const arns = new Set();
  for (const desiredStatus of ['RUNNING', 'STOPPED']) {
    const listed = await aws('ecs', 'list-tasks', {cluster, serviceName: asyncName, desiredStatus});
    requireCondition(!listed.nextToken && Array.isArray(listed.taskArns), 'Cannot identify NFT async tasks');
    listed.taskArns.forEach(arn => arns.add(arn));
  }
  requireCondition(arns.size <= 100 && (service.runningCount === 0 || arns.size > 0),
    'Cannot identify NFT async tasks');
  state.asyncTasks = [...arns];
  state.asyncStopAttempted = true;
  state.asyncReady = false;
  await persist();
  await aws('ecs', 'update-service', {cluster, service: asyncName, desiredCount: 0});
  await waitFactory(deps)('Actual async task shutdown', async () => {
    const current = await asyncService(aws);
    requireCondition(current.taskDefinition === revision, 'NFT async service revision changed');
    for (const desiredStatus of ['RUNNING', 'STOPPED']) {
      const listed = await aws('ecs', 'list-tasks', {cluster, serviceName: asyncName, desiredStatus});
      requireCondition(!listed.nextToken && Array.isArray(listed.taskArns), 'Cannot identify NFT async tasks');
      listed.taskArns.forEach(arn => arns.add(arn));
    }
    requireCondition(arns.size <= 100, 'Cannot identify NFT async tasks');
    state.asyncTasks = [...arns];
    await persist();
    return current.desiredCount === 0 && current.runningCount === 0 && current.pendingCount === 0 &&
      (await describeTasks(aws, state.asyncTasks)).every(task => task.lastStatus === 'STOPPED');
  });
  requireCondition(await scanQueueEmpty(aws), 'NFT scan queue changed during shutdown; refusing database replacement');
  state.asyncQuiescent = true;
  await persist();
}

export async function stopNftWriters(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, persist, log} = deps;
  requireCondition(state.maintenancePaused === true,
    'NFT maintenance schedules must be paused and drained before stopping writers');
  await identity(aws);
  requireCertificateCheckpoint(state, config);
  await noScheduledWriters(aws);
  const {app, simulator} = await servicesFactory(aws)();
  const asyncWorker = await asyncService(aws);
  requireCondition(simulator.desiredCount === 0 && simulator.runningCount === 0 && simulator.pendingCount === 0,
    'NFT simulator must be stopped');
  const {taskDefinition: restoreTask} = await aws('ecs', 'describe-task-definition', {taskDefinition: config.taskDefinition});
  const restoreDefinition = restoreDefinitionIdentity(restoreTask, config.taskDefinition);
  requireCondition(state.taskDefinition === undefined || state.taskDefinition === config.taskDefinition,
    'NFT recovery checkpoint belongs to another restore task revision');
  requireCondition(isDeepStrictEqual(state.restoreDefinition, restoreDefinition),
    'NFT recovery checkpoint has a different immutable dump or certificate identity');
  state.taskDefinition = config.taskDefinition;
  state.restoreDefinition = restoreDefinition;
  state.runId = config.runId;
  await persist();
  if (!state.originalApp) {
    requireCondition(app.taskDefinition.startsWith(`${taskPrefix}prsdb-webapp-nft:`) &&
      stable(app, app.taskDefinition, 1), 'NFT app baseline is not stable at one task');
    requireCondition(Number.isSafeInteger(asyncWorker.desiredCount) && asyncWorker.desiredCount >= 0 &&
      new RegExp(`^${taskPrefix}prsdb-async-worker-nft:\\d+$`).test(asyncWorker.taskDefinition ?? '') &&
      stable(asyncWorker, asyncWorker.taskDefinition, asyncWorker.desiredCount),
    'NFT async service baseline is not stable');
    const {taskDefinition: appDefinition} = await aws('ecs', 'describe-task-definition', {taskDefinition: app.taskDefinition});
    const {taskDefinition: originalAsync} = await aws('ecs', 'describe-task-definition', {taskDefinition: asyncWorker.taskDefinition});
    requireCondition(originalAsync?.taskDefinitionArn === asyncWorker.taskDefinition && originalAsync.status === 'ACTIVE',
      'Original async task is not runnable; refusing database replacement');
    if (config.expectedSha !== undefined) {
      state.resetCompatibility =
        await verifyResetCompatibility(aws, app, asyncWorker, appDefinition, originalAsync, config);
    }
    state.originalApp = {taskDefinition: app.taskDefinition, desiredCount: 1};
    state.originalAsync = {taskDefinition: asyncWorker.taskDefinition, desiredCount: asyncWorker.desiredCount};
    state.network = app.networkConfiguration.awsvpcConfiguration;
    state.taskDefinition = config.taskDefinition;
    state.runId = config.runId;
    state.expectedSha = config.expectedSha;
    state.resetSha = config.resetSha;
  } else {
    requireCondition((config.expectedSha === undefined || state.expectedSha === config.expectedSha) &&
      (config.resetSha === undefined || state.resetSha === config.resetSha),
      'NFT workflow compatibility identity changed during recovery');
    requireCondition(state.expectedSha === undefined || state.resetCompatibility,
      'NFT reset compatibility checkpoint is missing');
  }
  requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition,
    'Restore state belongs to another run or task definition');
  requireCondition(state.originalAsync, 'NFT async recovery state is missing');
  requireCondition(app.taskDefinition === state.originalApp.taskDefinition, 'NFT app revision changed');
  requireCondition([0, 1].includes(app.desiredCount), 'NFT app count changed externally');
  await stopAsyncWorker(deps);
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
  // ECS expires stopped-task records before a long Gatling run finishes.
  if (state.restorePassed === true && state.workerPassed === true && state.containers?.length === 1 &&
      state.containers[0].name === 'nft-vpc-restore' && state.containers[0].exitCode === 0) return;
  const [task] = await describeTasks(aws, [state.taskArn]);
  requireCondition(task.taskDefinitionArn === state.taskDefinition, 'Restore task definition changed');
  if (task.lastStatus !== 'STOPPED') {
    await aws('ecs', 'stop-task', {cluster, task: state.taskArn, reason: 'GitHub NFT restore cleanup'});
    await waitFactory({...deps, timeoutMs: 300000})('Restore task shutdown', async () =>
      (await describeTasks(aws, [state.taskArn]))[0].lastStatus === 'STOPPED', true);
  }
}

export async function cleanupNftEnvironment(config, loadReadiness, overrides = {}) {
  const deps = runtime(overrides);
  const {state} = deps;
  await identity(deps.aws);
  requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition,
    'Cleanup state belongs to another workflow');
  if (state.maintenanceScheduleSnapshot && !state.originalApp && !state.stopAttempted &&
      !state.launchAttempted && !state.restorePassed && !state.asyncRegistrationAttempted &&
      !state.asyncSwitchAttempted && !state.environmentRestored) {
    await restoreMaintenanceSchedules({...config, safeAbort: true}, deps);
    return;
  }
  requireCondition(state.maintenancePaused === true,
    'NFT maintenance schedules are not safely isolated; refusing cleanup and reset');
  const errors = [];
  try {
    await stopRestoreTask(deps);
  } catch (error) {
    errors.push(error);
  }
  if (state.originalAsync) await recoverAsyncRegistration(deps, true);
  if (state.asyncRegistrationAttempted) {
    // Stop the candidate before changing app/schema configuration, including failed starts.
    await stopAsyncWorker(deps);
    await returnAsyncRevision(deps);
  }
  if (state.environmentRestored !== true) {
    try {
      await restoreEnvironment(await loadReadiness(), {
        aws: deps.aws, log: deps.log, timeoutMs: 1200000, appHealthy: app => healthyAppTarget(deps.aws, app),
      });
      state.environmentRestored = true;
      await deps.persist();
    } catch (error) {
      errors.push(error);
    }
  }
  if (errors.length) throw new AggregateError(errors,
    `Cleanup incomplete; refusing database reset: ${errors.map(error => error.message).join('; ')}`);
  await stopNftWriters(config, deps);
  const latest = await deps.aws('ecs', 'list-task-definitions', {
    familyPrefix: 'prsdb-webapp-nft', status: 'ACTIVE', sort: 'DESC', maxResults: 1,
  });
  requireCondition(latest.taskDefinitionArns?.[0] === state.originalApp.taskDefinition,
    'Latest app revision is not the reset baseline; refusing automatic reset');
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
  requireCondition(state.maintenancePaused === true,
    'NFT maintenance schedules must remain paused through database replacement');
  await identity(aws);
  requireCondition(await scanQueueEmpty(aws), 'NFT scan queue is not empty; refusing database replacement');
  requireCondition(state.writersStopped && state.taskDefinition === config.taskDefinition &&
    state.runId === config.runId, 'App shutdown must complete before VPC restore');
  const {app, simulator} = await servicesFactory(aws)();
  const asyncWorker = await asyncService(aws);
  requireCondition(state.originalAsync && state.asyncStopAttempted && Array.isArray(state.asyncTasks) &&
    asyncWorker.taskDefinition === state.originalAsync.taskDefinition && asyncWorker.desiredCount === 0 &&
    asyncWorker.runningCount === 0 && asyncWorker.pendingCount === 0 &&
    (await describeTasks(aws, state.asyncTasks)).every(task => task.lastStatus === 'STOPPED'),
  'NFT async writer must remain stopped');
  requireCondition(app.taskDefinition === state.originalApp.taskDefinition && app.desiredCount === 0 &&
    app.runningCount === 0 && app.pendingCount === 0 && simulator.runningCount === 0 &&
    simulator.desiredCount === 0, 'NFT writers must remain stopped');
  requireCondition((await describeTasks(aws, state.appTasks)).every(task => task.lastStatus === 'STOPPED'),
    'App containers are still running');
  await noScheduledWriters(aws);
  const {taskDefinition} = await aws('ecs', 'describe-task-definition', {taskDefinition: config.taskDefinition});
  const restoreDefinition = restoreDefinitionIdentity(taskDefinition, config.taskDefinition);
  requireCondition(state.restoreDefinition !== undefined &&
    isDeepStrictEqual(state.restoreDefinition, restoreDefinition),
  'NFT restore task or immutable dump or certificate identity changed after compatibility checks');
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
  await checkCertificate(aws, state.restoreDefinition);
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

export async function restoreAsyncWorker(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, persist} = deps;
  await identity(aws);
  requireCondition(state.maintenancePaused === true,
    'NFT maintenance schedules must remain paused through async recovery');
  requireCondition(state.runId === config.runId && state.taskDefinition === config.taskDefinition &&
    state.originalAsync && Number.isSafeInteger(state.originalAsync.desiredCount) &&
    state.originalAsync.desiredCount >= 0 && state.asyncStopAttempted,
  'NFT async recovery state is missing or belongs to another run');
  const databaseChanged = state.launchAttempted || state.restorePassed || state.asyncSwitchAttempted || config.resetVerified;
  requireCondition(!databaseChanged || config.resetVerified === true,
    'Verified database reset is required before baseline worker recovery');
  requireCondition(!databaseChanged || state.qaVerified === true,
    'App migration and QA verification are required before baseline worker recovery');
  if (databaseChanged && state.originalAsync.desiredCount > 0) {
    const compatibility = state.resetCompatibility;
    requireCondition(/^[a-f0-9]{40}$/.test(state.resetSha ?? '') &&
      compatibility?.resetSha === state.resetSha &&
      compatibility.asyncTaskDefinition === state.originalAsync.taskDefinition &&
      compatibility.appTaskDefinition === state.originalApp?.taskDefinition &&
      compatibility.appSha === state.expectedSha &&
      (compatibility.mode === 'matching-workflow'
        ? compatibility.appSha === compatibility.resetSha &&
          compatibility.appImageDigest === compatibility.asyncImageDigest
        : compatibility.mode === 'explicit-pin' &&
          compatibility.compatibilityTaskDefinition === state.originalAsync.taskDefinition &&
          compatibility.compatibilitySha === state.resetSha),
    'Recorded app, worker, and reset schema compatibility is not verified');
  }
  await returnAsyncRevision(deps);
  const service = await asyncService(aws);
  requireCondition(service.taskDefinition === state.originalAsync.taskDefinition &&
    [0, state.originalAsync.desiredCount].includes(service.desiredCount), 'NFT async service changed');
  await aws('ecs', 'update-service', {cluster, service: asyncName, desiredCount: state.originalAsync.desiredCount});
  await waitFactory(deps)('Async service recovery', async () => {
    const current = await asyncService(aws);
    requireCondition(current.taskDefinition === state.originalAsync.taskDefinition, 'NFT async service changed');
    return stable(current, state.originalAsync.taskDefinition, state.originalAsync.desiredCount);
  }, true);
  state.asyncRestored = true;
  if (config.resetVerified === true) state.resetVerified = true;
  await persist();
}

async function returnAsyncRevision(deps) {
  const {aws, state, persist} = deps;
  await recoverAsyncRegistration(deps);
  const {taskDefinition: original} = await aws('ecs', 'describe-task-definition', {
    taskDefinition: state.originalAsync.taskDefinition,
  });
  requireCondition(original?.status === 'ACTIVE' && original.taskDefinitionArn === state.originalAsync.taskDefinition,
    'Original async task is not runnable; operator recovery required');
  let service = await asyncService(aws);
  requireCondition([state.originalAsync.taskDefinition, state.candidateAsync].includes(service.taskDefinition),
    'NFT async service changed');
  if (service.taskDefinition === state.candidateAsync) {
    requireCondition(state.asyncQuiescent && service.desiredCount === 0 &&
      service.runningCount === 0 && service.pendingCount === 0, 'Candidate async worker is not quiescent');
    await aws('ecs', 'update-service', {
      cluster, service: asyncName, taskDefinition: state.originalAsync.taskDefinition, desiredCount: 0,
    });
    await waitFactory(deps)('Async revision recovery', async () => {
      service = await asyncService(aws);
      requireCondition(service.taskDefinition === state.originalAsync.taskDefinition, 'NFT async service changed');
      return stable(service, state.originalAsync.taskDefinition, 0);
    }, true);
  }
  if (state.candidateAsync && !state.asyncRetired) {
    const {app, simulator} = await servicesFactory(aws)();
    service = await asyncService(aws);
    const listed = await aws('ecs', 'list-services', {cluster});
    const knownServices = [appName, 'nft-one-login-simulator', asyncName]
      .map(name => `arn:aws:ecs:${region}:${account}:service/${cluster}/${name}`);
    requireCondition(!listed.nextToken && Array.isArray(listed.serviceArns) &&
      listed.serviceArns.every(arn => knownServices.includes(arn)),
    'NFT service references cannot be established');
    requireCondition([app, simulator, service].every(value => value.taskDefinition !== state.candidateAsync &&
      !(value.deployments ?? []).some(deployment => deployment.taskDefinition === state.candidateAsync)),
    'Candidate worker task is still referenced');
    await aws('ecs', 'deregister-task-definition', {taskDefinition: state.candidateAsync});
    state.asyncRetired = true;
    await persist();
  }
}

export async function startAsyncWorker(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, persist, signal, log} = deps;
  await identity(aws);
  requireCondition(state.maintenancePaused === true,
    'NFT maintenance schedules must remain paused during performance preparation');
  requireCondition(state.restorePassed === true && state.runId === config.runId &&
    state.taskDefinition === config.taskDefinition && state.originalAsync && state.asyncStopAttempted,
  'Successful database restore and owned worker state are required');
  const {app} = await servicesFactory(aws)();
  requireCondition(stable(app, state.originalApp.taskDefinition, 1), 'Candidate app is not stable');
  const service = await asyncService(aws);
  requireCondition(stable(service, state.originalAsync.taskDefinition, 0), 'NFT async service changed before candidate start');
  const {taskDefinition: baseline} = await aws('ecs', 'describe-task-definition', {taskDefinition: state.originalAsync.taskDefinition});
  const {taskDefinition: candidate} = await aws('ecs', 'describe-task-definition', {taskDefinition: state.originalApp.taskDefinition});
  requireCondition(baseline?.taskDefinitionArn === state.originalAsync.taskDefinition &&
    candidate?.taskDefinitionArn === state.originalApp.taskDefinition, 'Candidate worker task identity mismatch');
  const cloned = cloneForScanWorker(baseline, candidate, config);
  cloned.containerDefinitions[0].image = await runningCandidateImage(aws, app, candidate);
  const {Parameter: queue} = await aws('ssm', 'get-parameter', {Name: 'nft-prsdb-scan-result-queue-name'});
  requireCondition(queue?.Value === scanQueueName && await scanQueueEmpty(aws), 'NFT scan queue is not ready');
  requireCondition(!state.asyncRegistrationAttempted, 'Candidate worker was already attempted; recover before retrying');
  signal?.throwIfAborted();
  state.asyncRegistrationAttempted = true;
  state.expectedSha = config.expectedSha;
  state.asyncImage = cloned.containerDefinitions[0].image;
  await persist();
  const {taskDefinition: registered} = await aws('ecs', 'register-task-definition', cloned);
  requireCondition(new RegExp(`^${taskPrefix}${cloned.family}:\\d+$`).test(registered?.taskDefinitionArn ?? ''),
    'Unexpected candidate worker task revision');
  state.candidateAsync = registered.taskDefinitionArn;
  await persist();
  requireCondition(stable(await asyncService(aws), state.originalAsync.taskDefinition, 0),
    'NFT async service changed before candidate switch');
  signal?.throwIfAborted();
  state.asyncSwitchAttempted = true;
  state.asyncQuiescent = false;
  state.writersStopped = false;
  await persist();
  await aws('ecs', 'update-service', {cluster, service: asyncName, taskDefinition: state.candidateAsync, desiredCount: 1});
  await waitFactory(deps)('Candidate scan worker readiness', async () => {
    const current = await asyncService(aws);
    requireCondition(current.taskDefinition === state.candidateAsync, 'Candidate scan worker changed');
    if (!stable(current, state.candidateAsync, 1)) return false;
    const listed = await aws('ecs', 'list-tasks', {cluster, serviceName: asyncName});
    if (listed.nextToken || listed.taskArns?.length !== 1) return false;
    const [task] = await describeTasks(aws, listed.taskArns);
    return task.taskDefinitionArn === state.candidateAsync && task.lastStatus === 'RUNNING' &&
      task.containers?.length === 1 && task.containers[0].name === 'prsdb-async-worker' &&
      task.containers[0].lastStatus === 'RUNNING' &&
      task.containers[0].imageDigest === state.asyncImage.split('@')[1] &&
      task.healthStatus !== 'UNHEALTHY';
  }, false, signal);
  state.asyncReady = true;
  await persist();
  log('Candidate scan worker ready: 1 running task.');
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
  const {aws, state, persist} = deps;
  await identity(aws);
  requireCondition(state.maintenancePaused === true,
    'NFT maintenance schedules must remain paused while the performance environment is active');
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
  state.appRestored = true;
  await persist();
}

export async function markQaVerified(config, overrides = {}) {
  validate(config);
  const deps = runtime(overrides);
  const {aws, state, persist} = deps;
  await identity(aws);
  requireCondition(config.resetVerified === true && state.runId === config.runId &&
    state.taskDefinition === config.taskDefinition && state.appRestored === true &&
    state.maintenancePaused === true,
  'Verified post-reset app state and QA action are required before maintenance recovery');
  state.qaVerified = true;
  await persist();
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const controller = new AbortController();
  const abort = () => controller.abort(new Error('NFT restore interrupted'));
  process.on('SIGTERM', abort);
  process.on('SIGINT', abort);
  try {
    const command = process.argv[2];
    if (['resolve-task-definition', 'snapshot-maintenance', 'stop', 'run', 'start', 'start-async'].includes(command)) {
      requireFreshAttempt();
    }
    const path = process.env.NFT_RESTORE_STATE_PATH ||
      (process.env.RUNNER_TEMP && join(process.env.RUNNER_TEMP, 'nft-restore-state.json'));
    requireCondition(path, 'NFT_RESTORE_STATE_PATH is required');
    const config = {
      taskDefinition: process.env.NFT_RESTORE_TASK_DEFINITION,
      runId: `${process.env.GITHUB_RUN_ID}-${process.env.GITHUB_RUN_ATTEMPT}`,
      expectedSha: process.env.EXPECTED_NFT_SHA,
      resetVerified: process.env.NFT_DATABASE_RESET_PASSED === 'true',
      resetSha: process.env.GITHUB_SHA,
      resetCompatibleTaskDefinition: process.env.NFT_RESET_COMPATIBLE_ASYNC_TASK_DEFINITION,
      resetCompatibleSha: process.env.NFT_RESET_COMPATIBLE_APP_SHA,
      recoveryVerified: process.env.NFT_MAINTENANCE_RECOVERY_VERIFIED === 'true',
    };
    const state = await readFile(path, 'utf8').then(JSON.parse).catch(error => {
      if (['resolve-task-definition', 'snapshot-maintenance', 'stop'].includes(command) && error.code === 'ENOENT') return {};
      throw new SafeRecoveryError('NFT restore state is missing or unreadable; manual recovery required');
    });
    if (command === 'start-async') config.taskDefinition = state.taskDefinition;
    if (['cleanup', 'pause-maintenance', 'restore-maintenance', 'mark-qa-verified', 'start-after-reset', 'restore-async'].includes(command)) {
      config.runId = recoveryOwner(state, {
        runId: process.env.GITHUB_RUN_ID, attempt: process.env.GITHUB_RUN_ATTEMPT,
      });
    }
    const checkpoint = ['pause-maintenance', 'restore-maintenance'].includes(command)
      ? () => uploadRecoveryCheckpoints(dirname(path), {
        runId: process.env.GITHUB_RUN_ID,
        attempt: process.env.GITHUB_RUN_ATTEMPT,
        phase: 'restore-state',
      })
      : async () => {};
    const deps = runtime({
      state, signal: controller.signal, checkpoint,
      persist: async () => {
        const temporary = `${path}.tmp`;
        await writeFile(temporary, JSON.stringify(state), {mode: 0o600});
        await rename(temporary, path);
      },
    });
    if (command === 'resolve-task-definition') {
      const taskDefinition = await resolveRestoreTaskDefinition(config, deps);
      requireCondition(process.env.GITHUB_OUTPUT, 'GITHUB_OUTPUT is required to publish the managed restore revision');
      await writeFile(process.env.GITHUB_OUTPUT, `task_definition=${taskDefinition}\n`, {flag: 'a'});
    }
    else if (command === 'snapshot-maintenance') await snapshotMaintenanceSchedules(config, deps);
    else if (command === 'pause-maintenance') await pauseMaintenanceSchedules(config, deps);
    else if (command === 'restore-maintenance') await restoreMaintenanceSchedules(config, deps);
    else if (command === 'mark-qa-verified') await markQaVerified(config, deps);
    else if (command === 'stop') {
      requireCondition(config.expectedSha, 'EXPECTED_NFT_SHA is required before stopping the app');
      await stopNftWriters(config, deps);
    }
    else if (command === 'run') await restoreInVpc(config, deps);
    else if (command === 'start') await startNftApp(deps);
    else if (command === 'start-async') await startAsyncWorker(config, deps);
    else if (command === 'start-after-reset') await startNftApp(deps, {afterReset: true});
    else if (command === 'restore-async') await restoreAsyncWorker(config, deps);
    else if (command === 'cleanup') {
      const readinessPath = process.env.READINESS_STATE_PATH ||
        (process.env.RUNNER_TEMP && join(process.env.RUNNER_TEMP, 'nft-state.json'));
      requireCondition(readinessPath, 'READINESS_STATE_PATH is required outside GitHub Actions');
      await cleanupNftEnvironment(config, async () => {
        return await readFile(readinessPath, 'utf8').then(JSON.parse).catch(error => {
          if (error.code === 'ENOENT' && process.env.READINESS_STATE_REQUIRED !== 'true' && !state.readinessAttempted) return null;
          throw new SafeRecoveryError('Readiness state cannot be read safely');
        });
      }, deps);
    } else throw new SafeRecoveryError('Unknown NFT VPC lifecycle operation');
  } catch (error) {
    if (error instanceof SafeRecoveryError) console.log(`NFT VPC restore details: ${error.message}`);
    console.error('NFT VPC operation failed; keep the app stopped until recovery succeeds.');
    process.exitCode = 1;
  } finally {
    process.off('SIGTERM', abort);
    process.off('SIGINT', abort);
  }
}

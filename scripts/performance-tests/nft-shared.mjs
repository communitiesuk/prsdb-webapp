import {execFile} from 'node:child_process';
import {promisify} from 'node:util';

const execute = promisify(execFile);

export const account = '448120078528';
export const region = 'eu-west-2';
export const cluster = 'nft-app';
export const appName = 'nft-app';
export const simulatorName = 'nft-one-login-simulator';
export const appOrigin = 'https://nft.register-home-to-rent.test.communities.gov.uk';
export const simulatorOrigin = 'https://nft.lb.register-home-to-rent.test.communities.gov.uk';
export const taskPrefix = `arn:aws:ecs:${region}:${account}:task-definition/`;
// The approved digest is owned by infra (terraform/nft/ecs_task_definition) and published here so this
// script has one source of truth to read at runtime, instead of a second hardcoded copy that can drift.
export const simulatorDigestParameterName = 'nft-one-login-simulator-approved-image-digest';

export function requireCondition(condition, message) {
  if (!condition) throw new Error(message);
}

export function environment(container) {
  return Object.fromEntries((container.environment ?? []).map(({name, value}) => [name, value]));
}

export function cloneForSimulator(task) {
  requireCondition(task.family === 'prsdb-webapp-nft', 'Unexpected app task family');
  const copy = structuredClone(task);
  const container = copy.containerDefinitions.find(value => value.name === 'prsdb-webapp');
  requireCondition(container, 'App container missing');
  const profile = container.environment?.find(value => value.name === 'SPRING_PROFILES_ACTIVE');
  requireCondition(profile && profile.value.split(',').map(value => value.trim()).sort().join(',') === 'default,nft',
    'Unexpected app baseline profile');
  profile.value += ',one-login-simulator';
  for (const key of ['taskDefinitionArn', 'revision', 'status', 'registeredAt', 'registeredBy',
    'requiresAttributes', 'compatibilities', 'deregisteredAt']) delete copy[key];
  return copy;
}

export async function awsCommand(service, operation, input = {}, awsRegion = region) {
  try {
    const {stdout} = await execute('aws', [service, operation, '--region', awsRegion, '--output', 'json',
      '--no-cli-pager', '--cli-input-json', JSON.stringify(input)], {timeout: 60000, maxBuffer: 4 * 1024 * 1024});
    return JSON.parse(stdout);
  } catch (error) {
    // AWS errors can echo task environment values. Report the operation and status, never raw stdout/stderr.
    const code = error.stderr?.match(/An error occurred \(([A-Za-z0-9]+)\)/)?.[1];
    const sanitized = new Error(`AWS ${service} ${operation} failed (status ${error.code ?? 'unknown'}${code ? `, ${code}` : ''})`);
    sanitized.awsCode = code;
    throw sanitized;
  }
}

export function servicesFactory(aws) {
  return async () => {
    const result = await aws('ecs', 'describe-services', {cluster, services: [appName, simulatorName]});
    requireCondition(!result.failures?.length && result.services?.length === 2, 'NFT services missing');
    const app = result.services.find(value => value.serviceName === appName);
    const simulator = result.services.find(value => value.serviceName === simulatorName);
    requireCondition(app?.status === 'ACTIVE' && simulator?.status === 'ACTIVE', 'NFT services are not active');
    return {app, simulator};
  };
}

export function stable(service, arn, desired) {
  return service.taskDefinition === arn && service.desiredCount === desired &&
    service.runningCount === desired && service.pendingCount === 0 && service.deployments?.length === 1 &&
    service.deployments[0].taskDefinition === arn && service.deployments[0].rolloutState === 'COMPLETED';
}

export function waitFactory({now, sleep, timeoutMs, pollMs}) {
  return async (description, check, cleanup = false, signal) => {
    const deadline = now() + timeoutMs;
    do {
      if (!cleanup) signal?.throwIfAborted();
      if (await check()) return;
      if (now() >= deadline) break;
      await sleep(pollMs);
    } while (now() <= deadline);
    throw new Error(`${description} timeout`);
  };
}

export function requestFactory(fetchImpl, log) {
  return async url => {
    try {
      return await fetchImpl(url, {redirect: 'manual', signal: AbortSignal.timeout(10000)});
    } catch (error) {
      if (error.name === 'TimeoutError' || error.name === 'TypeError') {
        log(`HTTP probe unavailable for ${new URL(url).pathname}; retrying within deadline.`);
        return null;
      }
      throw error;
    }
  };
}

export function ipSet(scope, id) {
  return {
    Name: scope === 'CLOUDFRONT' ? 'waf-performance-runner-cloudfront-nft' : 'waf-performance-runner-regional-nft',
    Scope: scope, Id: id,
  };
}

import {execFile} from 'node:child_process';
import {promisify} from 'node:util';
import {isIP} from 'node:net';
import {pathToFileURL} from 'node:url';

const execute = promisify(execFile);
const account = '448120078528';
const region = 'eu-west-2';
const cluster = 'nft-app';
const appName = 'nft-app';
const simulatorName = 'nft-one-login-simulator';
const appOrigin = 'https://nft.register-home-to-rent.test.communities.gov.uk';
const simulatorOrigin = 'https://nft.lb.register-home-to-rent.test.communities.gov.uk';
const taskPrefix = `arn:aws:ecs:${region}:${account}:task-definition/`;
export const simulatorTaskDefinitionDigest = 'sha256:5257554c6f6a50c471ad231bc8f13da4a866b4a2e320a6d1f2f74e5d0ad52755';

function requireCondition(condition, message) {
  if (!condition) throw new Error(message);
}

function environment(container) {
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

export async function runReadiness({runnerIp, runId, expectedSha, failSmoke = false}, {
  aws, fetchImpl = fetch, log = console.log, sleep = ms => new Promise(resolve => setTimeout(resolve, ms)),
  now = Date.now, timeoutMs = 600000, pollMs = 10000, signal,
}) {
  requireCondition(isIP(runnerIp) === 4, 'A public IPv4 runner address is required');
  requireCondition(/^\d+-\d+$/.test(runId), 'Expected GitHub run ID and attempt');
  const cidr = `${runnerIp}/32`;
  const marker = `PDJB-430:${runId}`;
  const ownedSets = [];
  let securityGroup;
  let ingressAttempted = false;
  let originalApp;
  let originalSimulator;
  let temporaryArn;
  let simulatorStarted = false;
  let appSwitchAttempted = false;
  let appSafe = true;
  let failure;
  const cleanupErrors = [];

  const services = async () => {
    const result = await aws('ecs', 'describe-services', {cluster, services: [appName, simulatorName]});
    requireCondition(!result.failures?.length && result.services?.length === 2, 'NFT services missing');
    const app = result.services.find(value => value.serviceName === appName);
    const simulator = result.services.find(value => value.serviceName === simulatorName);
    requireCondition(app?.status === 'ACTIVE' && simulator?.status === 'ACTIVE', 'NFT services are not active');
    return {app, simulator};
  };
  const stable = (service, arn, desired) => service.taskDefinition === arn && service.desiredCount === desired &&
    service.runningCount === desired && service.pendingCount === 0 && service.deployments?.length === 1 &&
    service.deployments[0].taskDefinition === arn && service.deployments[0].rolloutState === 'COMPLETED';
  const wait = async (description, check, cleanup = false) => {
    const deadline = now() + timeoutMs;
    do {
      if (!cleanup) signal?.throwIfAborted();
      if (await check()) return;
      if (now() >= deadline) break;
      await sleep(pollMs);
    } while (now() <= deadline);
    throw new Error(`${description} timeout`);
  };
  const request = async url => {
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
  const waitApp = async (arn, cleanup = false) => wait('App health', async () => {
    const {app} = await services();
    requireCondition(app.taskDefinition === arn, 'App task changed during health check');
    return stable(app, arn, originalApp.desiredCount) && (await request(`${appOrigin}/healthcheck`))?.status === 200;
  }, cleanup);
  const ipSet = (scope, id) => ({
    Name: scope === 'CLOUDFRONT' ? 'waf-performance-runner-cloudfront-nft' : 'waf-performance-runner-regional-nft',
    Scope: scope, Id: id,
  });
  const cleanup = async (description, action) => {
    try {
      await action();
    } catch (error) {
      cleanupErrors.push(new Error(`${description}: ${error.message}`));
      log(`Cleanup failed (${description}); operator recovery required.`);
    }
  };

  try {
    requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');
    const baseline = await services();
    originalApp = baseline.app;
    originalSimulator = baseline.simulator;
    requireCondition(originalApp.taskDefinition.startsWith(`${taskPrefix}prsdb-webapp-nft:`), 'Wrong app task family');
    requireCondition(originalSimulator.taskDefinition.startsWith(`${taskPrefix}prsdb-one-login-simulator-nft:`), 'Wrong simulator task family');
    requireCondition(stable(originalApp, originalApp.taskDefinition, 1), 'App baseline is not stable at one task');
    requireCondition(stable(originalSimulator, originalSimulator.taskDefinition, 0), 'Simulator baseline is not stopped');
    const {taskDefinition: appTask} = await aws('ecs', 'describe-task-definition', {taskDefinition: originalApp.taskDefinition});
    const {taskDefinition: simulatorTask} = await aws('ecs', 'describe-task-definition', {taskDefinition: originalSimulator.taskDefinition});
    const cloned = cloneForSimulator(appTask);
    requireCondition(appTask.taskRoleArn === `arn:aws:iam::${account}:role/nft-webapp-ecs-task` &&
      appTask.executionRoleArn === `arn:aws:iam::${account}:role/nft-ecs-task-execution`, 'Unexpected app task roles');
    const appContainer = appTask.containerDefinitions.find(value => value.name === 'prsdb-webapp');
    if (expectedSha !== undefined) {
      requireCondition(/^[a-f0-9]{40}$/.test(expectedSha) && appContainer.image.endsWith(`-${expectedSha}`),
        'NFT branch revision is not deployed; refusing to mark an older app ready');
    }
    const simulatorContainer = simulatorTask.containerDefinitions.find(value => value.name === 'prsdb-one-login-simulator');
    requireCondition(simulatorContainer?.image === `${account}.dkr.ecr.${region}.amazonaws.com/nft-one-login-simulator@${simulatorTaskDefinitionDigest}`,
      'Simulator service references an unapproved image; apply the image-pin release first');
    const appEnv = environment(appContainer);
    const simEnv = environment(simulatorContainer);
    requireCondition(simEnv.CLIENT_ID && simEnv.PUBLIC_KEY && !simEnv.PUBLIC_KEY.includes('default_to_be_set_manually') &&
      simEnv.CLIENT_ID === appEnv.ONE_LOGIN_SIMULATOR_CLIENT_ID && simEnv.PUBLIC_KEY === appEnv.ONE_LOGIN_SIMULATOR_PUBLIC_KEY,
      'Simulator credentials are missing, stale, or do not match the app');
    requireCondition(appContainer.secrets?.some(value => value.name === 'ONE_LOGIN_SIMULATOR_PRIVATE_KEY' &&
      value.valueFrom.startsWith(`arn:aws:secretsmanager:${region}:${account}:secret:tf-nft-one-login-simulator-private-key-`)),
      'Simulator private-key secret reference missing');
    requireCondition(appEnv.ONE_LOGIN_SIMULATOR_ISSUER_URL === `${simulatorOrigin}/` &&
      appEnv.ONE_LOGIN_SIMULATOR_DID_URL === `${simulatorOrigin}/.well-known/did.json` &&
      simEnv.SIMULATOR_URL === simulatorOrigin &&
      simEnv.REDIRECT_URLS === `${appOrigin}/login/oauth2/code/one-login` &&
      simEnv.POST_LOGOUT_REDIRECT_URLS === `${appOrigin}/signout` &&
      simEnv.PUBLIC_KEY_SOURCE === 'STATIC' && simEnv.TOKEN_AUTH_METHOD === 'private_key_jwt' &&
      simEnv.INTERACTIVE_MODE === 'true' && simEnv.IDENTITY_VERIFICATION_SUPPORTED === 'true',
      'Unexpected simulator static authentication configuration');
    const images = await aws('ecr', 'describe-images', {
      repositoryName: 'nft-one-login-simulator', imageIds: [{imageDigest: simulatorTaskDefinitionDigest}],
    });
    requireCondition(images.imageDetails?.some(value => value.imageDigest === simulatorTaskDefinitionDigest), 'Simulator image missing from ECR');
    log(`Original app task: ${originalApp.taskDefinition}; simulator task: ${originalSimulator.taskDefinition}; simulator count: 0.`);

    for (const scope of ['CLOUDFRONT', 'REGIONAL']) {
      const wafRegion = scope === 'CLOUDFRONT' ? 'us-east-1' : region;
      const listed = await aws('wafv2', 'list-ip-sets', {Scope: scope}, wafRegion);
      const name = ipSet(scope, '').Name;
      const matches = listed.IPSets?.filter(value => value.Name === name) ?? [];
      requireCondition(matches.length === 1, `Missing or ambiguous runner ${scope} IP set`);
      const input = ipSet(scope, matches[0].Id);
      const current = await aws('wafv2', 'get-ip-set', input, wafRegion);
      if (!current.IPSet.Addresses.includes(cidr)) {
        ownedSets.push({input, wafRegion});
        log(`Owned WAF addition: ${name} ${matches[0].Id} ${cidr}.`);
        try {
          await aws('wafv2', 'update-ip-set', {...input, Addresses: [...current.IPSet.Addresses, cidr],
            LockToken: current.LockToken}, wafRegion);
        } catch (error) {
          if (error.awsCode === 'WAFOptimisticLockException') ownedSets.pop();
          throw error;
        }
      }
    }
    const groups = await aws('ec2', 'describe-security-groups', {
      Filters: [{Name: 'group-name', Values: ['load-balancer-simulator-sg-nft']}],
    });
    requireCondition(groups.SecurityGroups?.length === 1, 'Simulator ALB security group missing or ambiguous');
    securityGroup = groups.SecurityGroups[0].GroupId;
    const rules = await aws('ec2', 'describe-security-group-rules', {Filters: [{Name: 'group-id', Values: [securityGroup]}]});
    if (!rules.SecurityGroupRules.some(value => !value.IsEgress && value.IpProtocol === 'tcp' &&
      value.FromPort === 443 && value.ToPort === 443 && value.CidrIpv4 === cidr)) {
      ingressAttempted = true;
      log(`Owned SG addition: ${securityGroup} ${cidr} ${marker}.`);
      await aws('ec2', 'authorize-security-group-ingress', {GroupId: securityGroup, IpPermissions: [{
        IpProtocol: 'tcp', FromPort: 443, ToPort: 443, IpRanges: [{CidrIp: cidr, Description: marker}],
      }]});
    }
    const beforeStart = await services();
    requireCondition(stable(beforeStart.app, originalApp.taskDefinition, 1) &&
      stable(beforeStart.simulator, originalSimulator.taskDefinition, 0), 'Services changed during preflight');
    signal?.throwIfAborted();
    simulatorStarted = true;
    await aws('ecs', 'update-service', {cluster, service: simulatorName, desiredCount: 1});
    await wait('Simulator health', async () => {
      const {simulator} = await services();
      requireCondition(simulator.taskDefinition === originalSimulator.taskDefinition, 'Simulator task changed');
      return stable(simulator, originalSimulator.taskDefinition, 1) && (await request(`${simulatorOrigin}/`))?.status === 200;
    });
    const beforeSwitch = await services();
    requireCondition(stable(beforeSwitch.app, originalApp.taskDefinition, 1), 'App task changed before simulator switch');
    signal?.throwIfAborted();
    log('Registering temporary app revision; if the response is lost, inspect family latest before any NFT deployment.');
    const registered = await aws('ecs', 'register-task-definition', cloned);
    const candidateArn = registered.taskDefinition?.taskDefinitionArn;
    requireCondition(candidateArn?.startsWith(`${taskPrefix}prsdb-webapp-nft:`) && candidateArn !== originalApp.taskDefinition,
      'Unexpected temporary task definition ARN');
    temporaryArn = candidateArn;
    log(`Temporary app task: ${temporaryArn}.`);
    requireCondition((await services()).app.taskDefinition === originalApp.taskDefinition, 'App task changed before update');
    signal?.throwIfAborted();
    appSwitchAttempted = true;
    appSafe = false;
    await aws('ecs', 'update-service', {cluster, service: appName, taskDefinition: temporaryArn});
    await waitApp(temporaryArn);
    if (failSmoke) throw new Error('Controlled smoke failure');
    const discovery = await request(`${simulatorOrigin}/.well-known/openid-configuration`);
    requireCondition(discovery?.status === 200, 'Simulator discovery failed');
    const document = await discovery.json();
    requireCondition(document.issuer === `${simulatorOrigin}/` && document.authorization_endpoint === `${simulatorOrigin}/authorize`,
      'Simulator discovery endpoints mismatch');
    const authorization = await request(`${appOrigin}/oauth2/authorization/one-login`);
    requireCondition(authorization?.status === 302, 'App authorization redirect missing');
    const location = new URL(authorization.headers.get('location'));
    requireCondition(location.origin === simulatorOrigin && location.pathname === '/authorize' &&
      location.searchParams.get('client_id') === simEnv.CLIENT_ID &&
      location.searchParams.get('redirect_uri') === `${appOrigin}/login/oauth2/code/one-login`,
      'App authorization redirect does not point at the configured simulator');
    log('Readiness passed: simulator discovery and app authorization redirect verified; login was not completed.');
  } catch (error) {
    failure = error;
  } finally {
    if (appSwitchAttempted) {
      await cleanup('restore app', async () => {
        const {app} = await services();
        requireCondition([originalApp.taskDefinition, temporaryArn].includes(app.taskDefinition),
          'Unexpected app task; refusing to overwrite another deployment');
        requireCondition(app.desiredCount === originalApp.desiredCount, 'App count changed externally');
        if (app.taskDefinition === temporaryArn) {
          await aws('ecs', 'update-service', {cluster, service: appName, taskDefinition: originalApp.taskDefinition});
        }
        await waitApp(originalApp.taskDefinition, true);
        appSafe = true;
        log(`Restored app task and health: ${originalApp.taskDefinition}.`);
      });
    }
    if (simulatorStarted && appSafe) {
      await cleanup('restore simulator', async () => {
        const {app, simulator} = await services();
        requireCondition(app.taskDefinition === originalApp.taskDefinition, 'App task changed; retaining simulator');
        requireCondition(simulator.taskDefinition === originalSimulator.taskDefinition &&
          [0, 1].includes(simulator.desiredCount), 'Simulator changed externally; refusing to overwrite');
        await aws('ecs', 'update-service', {cluster, service: simulatorName, desiredCount: 0});
        await wait('Simulator shutdown', async () => stable((await services()).simulator, originalSimulator.taskDefinition, 0), true);
        log('Restored simulator desired/running count: 0.');
      });
    }
    if (appSafe) {
      if (ingressAttempted) {
        await cleanup('remove owned SG rule', async () => {
          const {SecurityGroupRules} = await aws('ec2', 'describe-security-group-rules', {Filters: [{Name: 'group-id', Values: [securityGroup]}]});
          const ids = SecurityGroupRules.filter(value => value.Description === marker && value.CidrIpv4 === cidr &&
            !value.IsEgress && value.IpProtocol === 'tcp' && value.FromPort === 443 && value.ToPort === 443)
            .map(value => value.SecurityGroupRuleId);
          if (ids.length) await aws('ec2', 'revoke-security-group-ingress', {GroupId: securityGroup, SecurityGroupRuleIds: ids});
          await wait('Owned SG rule removal', async () => {
            const result = await aws('ec2', 'describe-security-group-rules', {Filters: [{Name: 'group-id', Values: [securityGroup]}]});
            return !result.SecurityGroupRules.some(value => value.Description === marker && value.CidrIpv4 === cidr);
          }, true);
        });
      }
      for (const {input, wafRegion} of ownedSets) {
        await cleanup(`remove owned ${input.Scope} CIDR`, async () => {
          const current = await aws('wafv2', 'get-ip-set', input, wafRegion);
          if (current.IPSet.Addresses.includes(cidr)) {
            await aws('wafv2', 'update-ip-set', {...input, Addresses: current.IPSet.Addresses.filter(value => value !== cidr),
              LockToken: current.LockToken}, wafRegion);
          }
          await wait(`Owned ${input.Scope} CIDR removal`, async () => {
            const result = await aws('wafv2', 'get-ip-set', input, wafRegion);
            return !result.IPSet.Addresses.includes(cidr);
          }, true);
        });
      }
      if (temporaryArn) {
        await cleanup('retire temporary task', async () => {
          const {app} = await services();
          requireCondition(app.taskDefinition === originalApp.taskDefinition &&
            !app.deployments.some(value => value.taskDefinition === temporaryArn), 'Temporary task still referenced');
          await aws('ecs', 'deregister-task-definition', {taskDefinition: temporaryArn});
          log(`Retired temporary app task: ${temporaryArn}.`);
        });
      }
    } else {
      log(`App restoration is unverified; retaining simulator/network access. Recover original app ${originalApp.taskDefinition} before stopping simulator.`);
    }
  }
  if (cleanupErrors.length) throw new AggregateError([...(failure ? [failure] : []), ...cleanupErrors],
    `Readiness cleanup failed: ${cleanupErrors.map(error => error.message).join('; ')}`);
  if (failure) throw failure;
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

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const controller = new AbortController();
  const abort = () => controller.abort(new Error('Readiness interrupted; restoring NFT'));
  process.on('SIGTERM', abort);
  process.on('SIGINT', abort);
  try {
    await runReadiness({
      runnerIp: process.env.RUNNER_IP,
      runId: `${process.env.GITHUB_RUN_ID}-${process.env.GITHUB_RUN_ATTEMPT}`,
      expectedSha: process.env.EXPECTED_NFT_SHA,
      failSmoke: process.env.FAIL_READINESS_SMOKE === 'true',
    }, {aws: awsCommand, signal: controller.signal});
  } catch (error) {
    console.error(error.message);
    process.exitCode = 1;
  } finally {
    process.off('SIGTERM', abort);
    process.off('SIGINT', abort);
  }
}

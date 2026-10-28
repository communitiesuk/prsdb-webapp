import {isIP} from 'node:net';
import {pathToFileURL} from 'node:url';
import {writeFile} from 'node:fs/promises';
import {
  account, appName, appOrigin, awsCommand, cloneForSimulator, cluster, environment, ipSet, region,
  requestFactory, requireCondition, servicesFactory, simulatorDigestParameterName, simulatorName,
  simulatorOrigin, stable, taskPrefix, waitFactory,
} from './nft-shared.mjs';

export async function prepareEnvironment({runnerIp, runId, expectedSha, failSmoke = false}, {
  aws, fetchImpl = fetch, log = console.log, sleep = ms => new Promise(resolve => setTimeout(resolve, ms)),
  now = Date.now, timeoutMs = 600000, pollMs = 10000, signal,
}) {
  requireCondition(isIP(runnerIp) === 4, 'A public IPv4 runner address is required');
  requireCondition(/^\d+-\d+$/.test(runId), 'Expected GitHub run ID and attempt');
  const cidr = `${runnerIp}/32`;
  const marker = `PDJB-430:${runId}`;
  const state = {
    cidr, marker, ownedSets: [], ingressAttempted: false, simulatorStarted: false, appSwitchAttempted: false,
    originalApp: null, originalSimulator: null, securityGroup: null, temporaryArn: null,
  };

  const services = servicesFactory(aws);
  const wait = waitFactory({now, sleep, timeoutMs, pollMs});
  const request = requestFactory(fetchImpl, log);
  const waitApp = (arn, cleanup = false) => wait('App health', async () => {
    const {app} = await services();
    requireCondition(app.taskDefinition === arn, 'App task changed during health check');
    return stable(app, arn, state.originalApp.desiredCount) && (await request(`${appOrigin}/healthcheck`))?.status === 200;
  }, cleanup, signal);

  try {
    requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');
    const baseline = await services();
    state.originalApp = {taskDefinition: baseline.app.taskDefinition, desiredCount: baseline.app.desiredCount};
    state.originalSimulator = {taskDefinition: baseline.simulator.taskDefinition, desiredCount: baseline.simulator.desiredCount};
    requireCondition(state.originalApp.taskDefinition.startsWith(`${taskPrefix}prsdb-webapp-nft:`), 'Wrong app task family');
    requireCondition(state.originalSimulator.taskDefinition.startsWith(`${taskPrefix}prsdb-one-login-simulator-nft:`), 'Wrong simulator task family');
    requireCondition(stable(baseline.app, state.originalApp.taskDefinition, 1), 'App baseline is not stable at one task');
    requireCondition(stable(baseline.simulator, state.originalSimulator.taskDefinition, 0), 'Simulator baseline is not stopped');
    const {taskDefinition: appTask} = await aws('ecs', 'describe-task-definition', {taskDefinition: state.originalApp.taskDefinition});
    const {taskDefinition: simulatorTask} = await aws('ecs', 'describe-task-definition', {taskDefinition: state.originalSimulator.taskDefinition});
    const cloned = cloneForSimulator(appTask);
    requireCondition(appTask.taskRoleArn === `arn:aws:iam::${account}:role/nft-webapp-ecs-task` &&
      appTask.executionRoleArn === `arn:aws:iam::${account}:role/nft-ecs-task-execution`, 'Unexpected app task roles');
    const appContainer = appTask.containerDefinitions.find(value => value.name === 'prsdb-webapp');
    if (expectedSha !== undefined) {
      requireCondition(/^[a-f0-9]{40}$/.test(expectedSha) && appContainer.image.endsWith(`-${expectedSha}`),
        'NFT branch revision is not deployed; refusing to mark an older app ready');
    }
    const simulatorContainer = simulatorTask.containerDefinitions.find(value => value.name === 'prsdb-one-login-simulator');
    const {Parameter: digestParameter} = await aws('ssm', 'get-parameter', {Name: simulatorDigestParameterName});
    const approvedDigest = digestParameter?.Value;
    requireCondition(/^sha256:[0-9a-f]{64}$/.test(approvedDigest ?? ''), 'Approved simulator digest parameter is missing or malformed');
    requireCondition(simulatorContainer?.image === `${account}.dkr.ecr.${region}.amazonaws.com/nft-one-login-simulator@${approvedDigest}`,
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
      repositoryName: 'nft-one-login-simulator', imageIds: [{imageDigest: approvedDigest}],
    });
    requireCondition(images.imageDetails?.some(value => value.imageDigest === approvedDigest), 'Simulator image missing from ECR');
    log(`Original app task: ${state.originalApp.taskDefinition}; simulator task: ${state.originalSimulator.taskDefinition}; simulator count: 0.`);

    for (const scope of ['CLOUDFRONT', 'REGIONAL']) {
      const wafRegion = scope === 'CLOUDFRONT' ? 'us-east-1' : region;
      const listed = await aws('wafv2', 'list-ip-sets', {Scope: scope}, wafRegion);
      const name = ipSet(scope, '').Name;
      const matches = listed.IPSets?.filter(value => value.Name === name) ?? [];
      requireCondition(matches.length === 1, `Missing or ambiguous runner ${scope} IP set`);
      const input = ipSet(scope, matches[0].Id);
      const current = await aws('wafv2', 'get-ip-set', input, wafRegion);
      if (!current.IPSet.Addresses.includes(cidr)) {
        state.ownedSets.push({input, wafRegion});
        log(`Owned WAF addition: ${name} ${matches[0].Id} ${cidr}.`);
        try {
          await aws('wafv2', 'update-ip-set', {...input, Addresses: [...current.IPSet.Addresses, cidr],
            LockToken: current.LockToken}, wafRegion);
        } catch (error) {
          if (error.awsCode === 'WAFOptimisticLockException') state.ownedSets.pop();
          throw error;
        }
      }
    }
    const groups = await aws('ec2', 'describe-security-groups', {
      Filters: [{Name: 'group-name', Values: ['load-balancer-simulator-sg-nft']}],
    });
    requireCondition(groups.SecurityGroups?.length === 1, 'Simulator ALB security group missing or ambiguous');
    state.securityGroup = groups.SecurityGroups[0].GroupId;
    const rules = await aws('ec2', 'describe-security-group-rules', {Filters: [{Name: 'group-id', Values: [state.securityGroup]}]});
    if (!rules.SecurityGroupRules.some(value => !value.IsEgress && value.IpProtocol === 'tcp' &&
      value.FromPort === 443 && value.ToPort === 443 && value.CidrIpv4 === cidr)) {
      state.ingressAttempted = true;
      log(`Owned SG addition: ${state.securityGroup} ${cidr} ${marker}.`);
      await aws('ec2', 'authorize-security-group-ingress', {GroupId: state.securityGroup, IpPermissions: [{
        IpProtocol: 'tcp', FromPort: 443, ToPort: 443, IpRanges: [{CidrIp: cidr, Description: marker}],
      }]});
    }
    const beforeStart = await services();
    requireCondition(stable(beforeStart.app, state.originalApp.taskDefinition, 1) &&
      stable(beforeStart.simulator, state.originalSimulator.taskDefinition, 0), 'Services changed during preflight');
    signal?.throwIfAborted();
    state.simulatorStarted = true;
    await aws('ecs', 'update-service', {cluster, service: simulatorName, desiredCount: 1});
    await wait('Simulator health', async () => {
      const {simulator} = await services();
      requireCondition(simulator.taskDefinition === state.originalSimulator.taskDefinition, 'Simulator task changed');
      return stable(simulator, state.originalSimulator.taskDefinition, 1) && (await request(`${simulatorOrigin}/`))?.status === 200;
    }, false, signal);
    const beforeSwitch = await services();
    requireCondition(stable(beforeSwitch.app, state.originalApp.taskDefinition, 1), 'App task changed before simulator switch');
    signal?.throwIfAborted();
    log('Registering temporary app revision; if the response is lost, inspect family latest before any NFT deployment.');
    const registered = await aws('ecs', 'register-task-definition', cloned);
    const candidateArn = registered.taskDefinition?.taskDefinitionArn;
    requireCondition(candidateArn?.startsWith(`${taskPrefix}prsdb-webapp-nft:`) && candidateArn !== state.originalApp.taskDefinition,
      'Unexpected temporary task definition ARN');
    state.temporaryArn = candidateArn;
    log(`Temporary app task: ${state.temporaryArn}.`);
    requireCondition((await services()).app.taskDefinition === state.originalApp.taskDefinition, 'App task changed before update');
    signal?.throwIfAborted();
    state.appSwitchAttempted = true;
    await aws('ecs', 'update-service', {cluster, service: appName, taskDefinition: state.temporaryArn});
    await waitApp(state.temporaryArn);
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
    return state;
  } catch (error) {
    // Attach whatever partial state was captured so the separate restore step can still clean up
    // exactly what this run owns, even though this step will not perform the cleanup itself.
    error.state = state;
    throw error;
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const controller = new AbortController();
  const abort = () => controller.abort(new Error('Prepare interrupted; restore will clean up'));
  process.on('SIGTERM', abort);
  process.on('SIGINT', abort);
  const statePath = process.env.READINESS_STATE_PATH;
  try {
    const state = await prepareEnvironment({
      runnerIp: process.env.RUNNER_IP,
      runId: `${process.env.GITHUB_RUN_ID}-${process.env.GITHUB_RUN_ATTEMPT}`,
      expectedSha: process.env.EXPECTED_NFT_SHA,
      failSmoke: process.env.FAIL_READINESS_SMOKE === 'true',
    }, {aws: awsCommand, signal: controller.signal});
    await writeFile(statePath, JSON.stringify(state));
  } catch (error) {
    await writeFile(statePath, JSON.stringify(error.state ?? null));
    // error.message is already sanitized (see awsCommand/requireCondition above); still, keep
    // caught error details off console.error/warn/trace (CWE-209) and log the summary there instead.
    console.log(`Prepare details: ${error.message}`);
    console.error('NFT environment preparation failed.');
    process.exitCode = 1;
  } finally {
    process.off('SIGTERM', abort);
    process.off('SIGINT', abort);
  }
}

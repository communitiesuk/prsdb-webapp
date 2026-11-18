import {spawn, spawnSync} from 'node:child_process';
import {chmod, mkdir, open, readdir} from 'node:fs/promises';
import {join} from 'node:path';
import {randomUUID} from 'node:crypto';
import {pathToFileURL} from 'node:url';

const bucket = 'prsdb-seed-data-nft';
const region = 'eu-west-2';
const heartbeatMs = 30_000;
const stages = new Set([
  'basicJourneys',
  'savedRegistrationJourneys',
  'completedPropertyUpdates',
  'organisationProfileUpdates',
  'certificateUploads',
  'prepareRampTeam',
  'prepareRampAgents',
  'prepareRampJoint',
  'rampedJourneys',
  'organisationRegistrations',
  'prepareOrganisationInvitations',
  'acceptOrganisationInvitations',
  'organisationAccessChanges',
  'operatorJourneys',
  'prepareAgentInvitations',
  'agentAccessJourneys',
  'prepareJointInvitations',
  'jointAcceptanceJourneys',
  'offboardingJourneys',
]);

function requireRunIdentity(env) {
  const {GITHUB_RUN_ID: runId, GITHUB_RUN_ATTEMPT: attempt, RUNNER_TEMP: runnerTemp} = env;
  if (!/^[1-9][0-9]*$/.test(runId ?? '') || !/^[1-9][0-9]*$/.test(attempt ?? '') || !runnerTemp) {
    throw new Error('Invalid run context');
  }
  return {runId, attempt, runnerTemp};
}

function signalExitCode(signal) {
  return ({SIGHUP: 129, SIGINT: 130, SIGTERM: 143})[signal] ?? 1;
}

function uploadDiagnostic(path, key, env) {
  const result = spawnSync('aws', [
    's3', 'cp', path, `s3://${bucket}/${key}`,
    '--only-show-errors', '--region', region,
  ], {env, stdio: 'ignore'});
  if (result.error || result.status !== 0) throw new Error('Private diagnostic upload failed');
}

export async function publishDiagnostics({env = process.env, log = line => console.log(line), now = Date.now} = {}) {
  const {runId, attempt, runnerTemp} = requireRunIdentity(env);
  const outputDirectory = join(runnerTemp, 'nft-gatling-diagnostics', `${runId}-${attempt}`);
  const startedAt = now();
  let entries;
  try {
    entries = await readdir(outputDirectory, {withFileTypes: true});
  } catch (error) {
    if (error.code === 'ENOENT') entries = [];
    else throw new Error('Private diagnostic files are unavailable');
  }

  const allowedStagePattern = [...stages].join('|');
  const filePattern = new RegExp(`^(${allowedStagePattern})-([0-9a-f-]{36})\\.(stdout|stderr)\\.log$`);
  const files = [];
  const pairs = new Map();
  for (const entry of entries) {
    const match = filePattern.exec(entry.name);
    if (!entry.isFile() || !match) throw new Error('Unexpected private diagnostic file');
    const stem = `${match[1]}-${match[2]}`;
    const pair = pairs.get(stem) ?? new Set();
    pair.add(match[3]);
    pairs.set(stem, pair);
    files.push({path: join(outputDirectory, entry.name), key: `performance-recovery/${runId}/${attempt}/gatling/${match[1]}/${entry.name}`});
  }
  if ([...pairs.values()].some(pair => pair.size !== 2)) throw new Error('Incomplete private diagnostics');

  let uploadedFileCount = 0;
  let uploadFailed = false;
  for (const file of files) {
    try {
      uploadDiagnostic(file.path, file.key, env);
      uploadedFileCount++;
    } catch {
      uploadFailed = true;
    }
  }
  const elapsedSeconds = Math.max(0, Math.floor((now() - startedAt) / 1000));
  const exitCode = uploadFailed ? 1 : 0;
  const status = uploadFailed ? 'failed' : 'complete';
  log(`NFT_GATLING_DIAGNOSTICS status=${status} elapsed_seconds=${elapsedSeconds} uploaded_file_count=${uploadedFileCount} exit_code=${exitCode}`);
  return {exitCode, uploadedFileCount, status};
}

function launchProcess(command, args, env, stdout, stderr, cwd) {
  return new Promise(resolve => {
    let child;
    let finished = false;
    let requestedSignal;
    const finish = result => {
      if (finished) return;
      finished = true;
      process.removeListener('SIGINT', onInterrupt);
      process.removeListener('SIGTERM', onTerminate);
      resolve({...result, requestedSignal});
    };
    const interrupt = signal => {
      requestedSignal = signal;
      try {
        child?.kill(signal);
      } catch {
        requestedSignal = signal;
      }
    };
    const onInterrupt = () => interrupt('SIGINT');
    const onTerminate = () => interrupt('SIGTERM');

    process.once('SIGINT', onInterrupt);
    process.once('SIGTERM', onTerminate);
    try {
      child = spawn(command, args, {
        cwd,
        env,
        stdio: ['ignore', stdout.fd, stderr.fd],
      });
    } catch {
      finish({code: 127, signal: null});
      return;
    }
    child.once('error', () => finish({code: 127, signal: null}));
    child.once('close', (code, signal) => finish({code, signal}));
  });
}

export async function runGatlingStage({
  stage,
  command,
  args,
  env = process.env,
  cwd = process.cwd(),
  heartbeatMs: heartbeatInterval = heartbeatMs,
  log = line => console.log(line),
  now = Date.now,
}) {
  if (!stages.has(stage) || command !== './gradlew' || !Array.isArray(args) || args[0] !== 'gatlingRun') {
    throw new Error('Invalid launch');
  }

  const {runId, attempt, runnerTemp} = requireRunIdentity(env);
  const outputDirectory = join(runnerTemp, 'nft-gatling-diagnostics', `${runId}-${attempt}`);
  await mkdir(outputDirectory, {recursive: true, mode: 0o700});
  await chmod(outputDirectory, 0o700);
  const launchId = randomUUID();
  const stdoutPath = join(outputDirectory, `${stage}-${launchId}.stdout.log`);
  const stderrPath = join(outputDirectory, `${stage}-${launchId}.stderr.log`);
  const stdout = await open(stdoutPath, 'wx', 0o600);
  let stderr;
  try {
    stderr = await open(stderrPath, 'wx', 0o600);
  } catch (error) {
    await stdout.close();
    throw error;
  }

  const startedAt = now();
  const elapsedSeconds = () => Math.max(0, Math.floor((now() - startedAt) / 1000));
  log(`NFT_GATLING stage=${stage} status=started elapsed_seconds=0`);
  const heartbeat = setInterval(() => {
    log(`NFT_GATLING stage=${stage} status=heartbeat elapsed_seconds=${elapsedSeconds()}`);
  }, heartbeatInterval);

  let childResult;
  try {
    childResult = await launchProcess(command, args, env, stdout, stderr, cwd);
  } finally {
    clearInterval(heartbeat);
    await Promise.all([stdout.close(), stderr.close()]);
  }

  const childExitCode = childResult.code ?? signalExitCode(childResult.signal);
  const aborted = Boolean(childResult.signal || childResult.requestedSignal);
  const exitCode = childExitCode;
  const status = aborted ? 'aborted' : exitCode === 0 ? 'complete' : 'failed';
  log(`NFT_GATLING stage=${stage} status=${status} elapsed_seconds=${elapsedSeconds()} exit_code=${exitCode}`);
  return {exitCode, status};
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const [, , action, ...args] = process.argv;
    let result;
    if (action === 'publish') {
      if (args.length) throw new Error('Invalid launch');
      result = await publishDiagnostics();
    } else {
      const [separator, command, ...launchArgs] = args;
      if (separator !== '--') throw new Error('Invalid launch');
      result = await runGatlingStage({stage: action, command, args: launchArgs});
    }
    process.exitCode = result.exitCode;
  } catch {
    console.log('NFT_GATLING status=invalid-launch elapsed_seconds=0 exit_code=1');
    process.exitCode = 1;
  }
}

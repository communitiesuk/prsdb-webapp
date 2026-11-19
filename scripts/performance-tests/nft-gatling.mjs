import {spawn, spawnSync} from 'node:child_process';
import {chmod, cp, lstat, mkdir, open, readdir} from 'node:fs/promises';
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

function reportError(status) {
  return Object.assign(new Error('Gatling report capture failed'), {reportStatus: status});
}

async function reportEntries(directory) {
  let info;
  try {
    info = await lstat(directory);
  } catch (error) {
    if (error.code === 'ENOENT') return [];
    throw error;
  }
  if (!info.isDirectory() || info.isSymbolicLink()) throw reportError('unsafe');
  const entries = await readdir(directory, {withFileTypes: true});
  if (entries.some(entry => !entry.isDirectory() || entry.isSymbolicLink())) throw reportError('unsafe');
  return entries;
}

async function validateReportTree(directory) {
  const root = await lstat(directory);
  if (!root.isDirectory() || root.isSymbolicLink()) throw reportError('unsafe');
  let hasIndex = false;
  let hasSimulationLog = false;
  async function visit(current, atRoot = false) {
    for (const entry of await readdir(current, {withFileTypes: true})) {
      const path = join(current, entry.name);
      const info = await lstat(path);
      if (info.isSymbolicLink() || (!info.isDirectory() && !info.isFile())) throw reportError('unsafe');
      if (info.isDirectory()) await visit(path);
      else if (atRoot && entry.name === 'index.html') hasIndex = true;
      else if (atRoot && entry.name === 'simulation.log') hasSimulationLog = true;
    }
  }
  await visit(directory, true);
  if (!hasIndex || !hasSimulationLog) throw reportError('missing');
}

async function retainStageReport({cwd, outputDirectory, stage, previousReports}) {
  const sourceDirectory = join(cwd, 'build', 'reports', 'gatling');
  const entries = await reportEntries(sourceDirectory);
  const generated = entries.filter(entry => !previousReports.has(entry.name));
  if (generated.length !== 1 || !/^[A-Za-z0-9][A-Za-z0-9._-]*$/.test(generated[0]?.name ?? '')) {
    throw reportError(generated.length ? 'unsafe' : 'missing');
  }
  const source = join(sourceDirectory, generated[0].name);
  await validateReportTree(source);
  const reportsDirectory = join(outputDirectory, 'reports');
  await mkdir(reportsDirectory, {recursive: true, mode: 0o700});
  const destination = join(reportsDirectory, stage);
  await cp(source, destination, {recursive: true, errorOnExist: true, force: false});
  await validateReportTree(destination);
}

async function createReportArchive(outputDirectory, diagnosticsDirectory) {
  const reportsDirectory = join(outputDirectory, 'reports');
  const entries = await reportEntries(reportsDirectory);
  if (!entries.length) return undefined;
  for (const entry of entries) {
    if (!stages.has(entry.name)) throw reportError('unsafe');
    await validateReportTree(join(reportsDirectory, entry.name));
  }
  const archive = join(diagnosticsDirectory, 'reports.tar.gz');
  let handle;
  try {
    handle = await open(archive, 'wx', 0o600);
    await handle.close();
    const result = spawnSync('tar', ['-czf', archive, '-C', outputDirectory, 'reports'], {stdio: 'ignore'});
    if (result.error || result.status !== 0) throw reportError('failed');
    await chmod(archive, 0o600);
    return archive;
  } catch (error) {
    if (handle) await handle.close().catch(() => {});
    throw error.reportStatus ? error : reportError('failed');
  }
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
    if ((entry.name === 'reports' && entry.isDirectory()) ||
        (entry.name === 'reports.tar.gz' && entry.isFile())) continue;
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
  let reportArchiveStatus = 'none';
  try {
    const archive = await createReportArchive(outputDirectory, outputDirectory);
    if (archive) {
      files.push({
        path: archive,
        key: `performance-recovery/${runId}/${attempt}/gatling/reports.tar.gz`,
        reportArchive: true,
      });
      reportArchiveStatus = 'complete';
    } else if (files.length) {
      reportArchiveStatus = 'missing';
      uploadFailed = true;
    }
  } catch (error) {
    reportArchiveStatus = error.reportStatus ?? 'failed';
    uploadFailed = true;
  }
  for (const file of files) {
    try {
      uploadDiagnostic(file.path, file.key, env);
      uploadedFileCount++;
    } catch {
      uploadFailed = true;
      if (file.reportArchive) reportArchiveStatus = 'failed';
    }
  }
  const elapsedSeconds = Math.max(0, Math.floor((now() - startedAt) / 1000));
  const exitCode = uploadFailed ? 1 : 0;
  const status = uploadFailed ? 'failed' : 'complete';
  log(`NFT_GATLING_DIAGNOSTICS status=${status} elapsed_seconds=${elapsedSeconds} uploaded_file_count=${uploadedFileCount} exit_code=${exitCode} report_archive_status=${reportArchiveStatus}`);
  return {exitCode, uploadedFileCount, reportArchiveStatus, status};
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
  const reportsDirectory = join(cwd, 'build', 'reports', 'gatling');
  const previousReports = new Set((await reportEntries(reportsDirectory)).map(entry => entry.name));
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
    childResult = await launchProcess(command, args, {...env, NFT_GATLING_STAGE: stage}, stdout, stderr, cwd);
  } finally {
    clearInterval(heartbeat);
    await Promise.all([stdout.close(), stderr.close()]);
  }

  const gradleExitCode = childResult.code ?? signalExitCode(childResult.signal);
  const aborted = Boolean(childResult.signal || childResult.requestedSignal);
  let reportStatus;
  try {
    await retainStageReport({cwd, outputDirectory, stage, previousReports});
    reportStatus = 'complete';
  } catch (error) {
    reportStatus = error.reportStatus ?? 'failed';
  }
  const exitCode = gradleExitCode === 0 && reportStatus !== 'complete' ? 1 : gradleExitCode;
  const status = aborted ? 'aborted' : exitCode === 0 ? 'complete' : 'failed';
  log(`NFT_GATLING stage=${stage} status=${status} elapsed_seconds=${elapsedSeconds()} exit_code=${exitCode}`);
  log(`NFT_GATLING stage=${stage} gradle_exit_code=${gradleExitCode} report_status=${reportStatus} exit_code=${exitCode}`);
  return {exitCode, gradleExitCode, reportStatus, status};
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

import {execFileSync} from 'node:child_process';
import {mkdir, readFile, readdir, rename, writeFile} from 'node:fs/promises';
import {join} from 'node:path';
import {pathToFileURL} from 'node:url';
import {formatSafeError} from './nft-shared.mjs';

export class SafeRecoveryError extends Error {
  constructor(message) {
    super(message);
    this.name = 'SafeRecoveryError';
    this.safeToLog = true;
  }
}

function requireCondition(condition, message) {
  if (!condition) throw new SafeRecoveryError(message);
}

const recoveryPrefix = 'performance-recovery';
const defaultBucket = 'prsdb-seed-data-nft';
const phaseFiles = {
  'restore-checkpoint': ['nft-restore-state.json'],
  'restore-state': ['nft-restore-state.json'],
  'load-checkpoint': ['nft-restore-state.json'],
  'worker-state': ['nft-restore-state.json'],
  'readiness-state': ['nft-state.json'],
  'cleanup-state': ['nft-restore-state.json', 'nft-state.json'],
};

function context({runId, attempt}) {
  requireCondition(/^[1-9][0-9]*$/.test(runId ?? ''), 'Expected a GitHub recovery run ID');
  const parsed = Number(attempt);
  requireCondition(Number.isSafeInteger(parsed) && parsed > 0, 'Expected a positive recovery attempt');
  return {runId, attempt: parsed};
}

function environment(env) {
  return context({runId: env.GITHUB_RUN_ID, attempt: env.GITHUB_RUN_ATTEMPT});
}

function storageContext(options) {
  const current = context(options);
  const bucket = options.bucket ?? defaultBucket;
  requireCondition(bucket === defaultBucket,
    'Private recovery storage is not configured');
  return {...current, bucket};
}

function runAws(args, runner) {
  try {
    return runner('aws', args, {encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe']});
  } catch (error) {
    const operation = /^[a-z0-9-]+$/.test(args[1] ?? '') ? args[1] : 'operation';
    const status = Number.isSafeInteger(error?.code) ? String(error.code) :
      /^[A-Z0-9_-]+$/.test(error?.code ?? '') ? error.code : 'unknown';
    const code = error?.stderr?.match(/An error occurred \(([A-Za-z0-9]+)\)/)?.[1];
    throw new SafeRecoveryError(
      `Private recovery ${operation} failed (status ${status}${code ? `, ${code}` : ''})`,
    );
  }
}

function s3Prefix(runId) {
  return `${recoveryPrefix}/${runId}/`;
}

function s3Key(runId, attempt, phase, file) {
  return `${s3Prefix(runId)}${attempt}/${phase}/${file}`;
}

function artifactDirectory(phase, runId, attempt) {
  return `nft-${phase}-${runId}-${attempt}`;
}

export function recoveryOwner(state, options) {
  const current = context(options);
  const owner = /^([1-9][0-9]*)-([1-9][0-9]*)$/.exec(state?.runId ?? '');
  requireCondition(owner && owner[1] === current.runId &&
    Number.isSafeInteger(Number(owner[2])) && Number(owner[2]) <= current.attempt,
  'Recovery state belongs to another run or a future attempt');
  return state.runId;
}

export function requireFreshAttempt(env = process.env) {
  requireCondition(environment(env).attempt === 1,
    'Performance retries require a fresh workflow run and seed restore; recovery jobs may be retried');
}

async function json(path) {
  try {
    return JSON.parse(await readFile(path, 'utf8'));
  } catch {
    throw new SafeRecoveryError('Recovery checkpoint is missing or malformed');
  }
}

async function save(path, state) {
  await writeFile(`${path}.tmp`, `${JSON.stringify(state)}\n`, {mode: 0o600});
  await rename(`${path}.tmp`, path);
}

export async function markReadinessAttempted(path, env = process.env) {
  requireFreshAttempt(env);
  const state = await json(path);
  recoveryOwner(state, environment(env));
  state.readinessAttempted = true;
  await save(path, state);
}

export async function uploadRecoveryCheckpoints(directory, options, runner = execFileSync) {
  const {runId, attempt, bucket} = storageContext(options);
  const {phase} = options;
  const files = Object.hasOwn(phaseFiles, phase) ? phaseFiles[phase] : undefined;
  requireCondition(files, 'Unknown recovery checkpoint phase');
  for (const file of files) {
    runAws([
      's3', 'cp', join(directory, file), `s3://${bucket}/${s3Key(runId, attempt, phase, file)}`,
      '--only-show-errors', '--region', 'eu-west-2',
    ], runner);
  }
}

export async function downloadRecoveryCheckpoints(directory, options, runner = execFileSync) {
  const {runId, attempt, bucket} = storageContext(options);
  const prefix = s3Prefix(runId);
  const output = runAws([
    's3api', 'list-objects-v2', '--bucket', bucket, '--prefix', prefix,
    '--query', 'Contents[].Key', '--output', 'json', '--region', 'eu-west-2',
  ], runner);
  let keys;
  try {
    keys = JSON.parse(output || '[]');
  } catch {
    throw new SafeRecoveryError('Private recovery checkpoint listing was invalid');
  }
  if (keys === null) keys = [];
  requireCondition(Array.isArray(keys), 'Private recovery checkpoint listing was invalid');
  for (const key of keys) {
    if (typeof key !== 'string') throw new SafeRecoveryError('Private recovery checkpoint listing was invalid');
    const [root, ownerRunId, rawAttempt, phase, file, ...extra] = key.split('/');
    const parsedAttempt = Number(rawAttempt);
    if (root !== recoveryPrefix || ownerRunId !== runId || extra.length ||
      !/^[1-9][0-9]*$/.test(rawAttempt) || !Number.isSafeInteger(parsedAttempt) ||
      parsedAttempt > attempt || !Object.hasOwn(phaseFiles, phase) || !phaseFiles[phase].includes(file)) continue;
    const targetDirectory = join(directory, artifactDirectory(phase, runId, parsedAttempt));
    await mkdir(targetDirectory, {recursive: true, mode: 0o700});
    runAws([
      's3', 'cp', `s3://${bucket}/${key}`, join(targetDirectory, file),
      '--only-show-errors', '--region', 'eu-west-2',
    ], runner);
  }
}

export async function selectRecoveryState(directory, {runId, attempt, outputDirectory}) {
  const current = context({runId, attempt});
  const phases = {'restore-checkpoint': 1, 'restore-state': 2, 'worker-state': 2.5, 'load-checkpoint': 3, 'cleanup-state': 4, 'readiness-state': 0};
  const candidates = [];
  for (const entry of await readdir(directory, {withFileTypes: true})) {
    const match = /^nft-(restore-checkpoint|restore-state|worker-state|load-checkpoint|cleanup-state|readiness-state)-([1-9][0-9]*)-([1-9][0-9]*)$/.exec(entry.name);
    if (!entry.isDirectory() || !match) continue;
    requireCondition(match[2] === current.runId && Number.isSafeInteger(Number(match[3])) &&
      Number(match[3]) <= current.attempt, 'Recovery checkpoint belongs to another run or a future attempt');
    candidates.push({path: join(directory, entry.name), phase: match[1], attempt: Number(match[3])});
  }
  candidates.sort((a, b) => b.attempt - a.attempt || phases[b.phase] - phases[a.phase]);
  const restore = candidates.find(value => value.phase !== 'readiness-state');
  requireCondition(restore, 'No restore checkpoint is available; manual recovery required');
  const state = await json(join(restore.path, 'nft-restore-state.json'));
  recoveryOwner(state, {runId, attempt: restore.attempt});

  let readiness;
  let readinessFound = false;
  for (const candidate of candidates) {
    if (!['readiness-state', 'cleanup-state'].includes(candidate.phase)) continue;
    const path = join(candidate.path, 'nft-state.json');
    try {
      await readFile(path);
    } catch (error) {
      if (error.code === 'ENOENT' && candidate.phase === 'cleanup-state') continue;
      throw new SafeRecoveryError('Readiness checkpoint is missing or unreadable');
    }
    readiness = await json(path);
    readinessFound = true;
    requireCondition(readiness === null || readiness.marker === `PDJB-430:${state.runId}`,
      'Readiness state belongs to another restore owner');
    break;
  }
  requireCondition(!state.readinessAttempted || readinessFound,
    'Required readiness checkpoint is missing; manual recovery required');
  await mkdir(outputDirectory, {recursive: true});
  await save(join(outputDirectory, 'nft-restore-state.json'), state);
  await save(join(outputDirectory, 'nft-state.json'), readiness ?? null);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const command = process.argv[2];
    if (command === 'require-fresh-attempt') requireFreshAttempt();
    else {
      const directory = process.env.RUNNER_TEMP;
      requireCondition(directory, 'RUNNER_TEMP is required for recovery checkpoints');
      const current = {...environment(process.env), bucket: process.env.NFT_RECOVERY_BUCKET ?? defaultBucket};
      if (command === 'mark-readiness') {
        await markReadinessAttempted(join(directory, 'nft-restore-state.json'));
      } else if (command === 'upload') {
        await uploadRecoveryCheckpoints(directory, {...current, phase: process.argv[3]});
      } else if (command === 'download') {
        await downloadRecoveryCheckpoints(join(directory, 'nft-recovery-artifacts'), current);
      } else if (command === 'select') {
        await selectRecoveryState(join(directory, 'nft-recovery-artifacts'), {
          ...current, outputDirectory: directory,
        });
      } else throw new SafeRecoveryError('Unknown recovery operation');
    }
  } catch (error) {
    console.error(`NFT recovery details: ${formatSafeError(error)}`);
    console.error('NFT recovery checkpoint operation failed; operator recovery may be required.');
    process.exitCode = 1;
  }
}

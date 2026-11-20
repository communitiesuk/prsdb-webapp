import assert from 'node:assert/strict';
import {chmod, mkdtemp, mkdir, readFile, rm, writeFile} from 'node:fs/promises';
import {spawnSync} from 'node:child_process';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {fileURLToPath} from 'node:url';
import {test} from 'node:test';

const workflowPath = new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url);
const recoveryScript = fileURLToPath(new URL('../../../scripts/performance-tests/nft-recovery-state.mjs', import.meta.url));
const marker = 'SYNTHETIC_PRIVATE_RECOVERY_MARKER_NOT_A_CREDENTIAL';

async function setup(t) {
  const directory = await mkdtemp(join(tmpdir(), 'nft-recovery-state-'));
  t.after(() => rm(directory, {recursive: true, force: true}));
  const bin = join(directory, 'bin');
  await mkdir(bin);
  const log = join(directory, 'aws-arguments.jsonl');
  const runner = join(bin, 'aws');
  await writeFile(runner, `#!/usr/bin/env node
const {appendFileSync, mkdirSync, writeFileSync} = require('node:fs');
const {dirname} = require('node:path');
const args = process.argv.slice(2);
appendFileSync(process.env.AWS_FAKE_ARGUMENTS, \`\${JSON.stringify(args)}\\n\`);
process.stderr.write('${marker}');
if (args[0] === 's3api') {
  process.stdout.write(process.env.AWS_FAKE_KEYS ?? '[]');
} else if (args[0] === 's3' && args[1] === 'cp' && args[2].startsWith('s3://')) {
  const destination = args[3];
  mkdirSync(dirname(destination), {recursive: true});
  const content = destination.endsWith('nft-state.json')
    ? process.env.AWS_FAKE_READINESS
    : process.env.AWS_FAKE_STATE;
  writeFileSync(destination, content);
}
if (args[0] !== 's3api') process.stdout.write('${marker}');
if (process.env.AWS_FAKE_FAILURE === 'true') process.exit(1);
`);
  await chmod(runner, 0o700);
  return {directory, bin, log};
}

function runRecovery(setupResult, args, extraEnv = {}) {
  return spawnSync(process.execPath, [recoveryScript, ...args], {
    encoding: 'utf8',
    env: {
      ...process.env,
      PATH: `${setupResult.bin}:${process.env.PATH}`,
      RUNNER_TEMP: setupResult.directory,
      GITHUB_RUN_ID: '123',
      GITHUB_RUN_ATTEMPT: '2',
      AWS_ACCESS_KEY_ID: 'dummy',
      AWS_SECRET_ACCESS_KEY: 'dummy',
      AWS_SESSION_TOKEN: 'dummy',
      AWS_EC2_METADATA_DISABLED: 'true',
      AWS_ENDPOINT_URL: 'http://127.0.0.1:9',
      AWS_FAKE_ARGUMENTS: setupResult.log,
      AWS_FAKE_STATE: JSON.stringify({runId: '123-1', readinessAttempted: true}),
      AWS_FAKE_READINESS: JSON.stringify({marker: 'PDJB-430:123-1'}),
      ...extraEnv,
    },
  });
}

test('public artifacts contain no raw recovery state or Gatling report', async () => {
  const workflow = await readFile(workflowPath, 'utf8');
  const artifactSteps = workflow.split(/(?=^\s+- (?:name|uses):)/m)
    .filter(step => /uses: actions\/upload-artifact@/.test(step));
  const unsafeArtifacts = artifactSteps.filter(step =>
    /nft-(?:restore|load|worker|readiness|cleanup)-|nft-state\.json|nft-restore-state\.json|gatling-report|build\/reports\/gatling/.test(step));
  const artifactNames = artifactSteps.map(step => /^\s*name:\s*(.+)$/m.exec(step)?.[1].trim());

  assert.equal(unsafeArtifacts.length, 0, 'public artifacts must only contain allowlisted outputs');
  assert.deepEqual(artifactNames, ['gatling-classes-${{ github.run_id }}', 'nft-head-sha']);
});

test('successful private checkpoint upload does not emit the state marker', async t => {
  const state = await setup(t);
  await writeFile(join(state.directory, 'nft-restore-state.json'),
    JSON.stringify({taskPrivateNetworkMetadata: marker}), {mode: 0o600});

  const result = runRecovery(state, ['upload', 'restore-state']);
  const calls = (await readFile(state.log, 'utf8')).trim().split('\n').map(line => JSON.parse(line));

  assert.equal(result.status, 0);
  assert.doesNotMatch(result.stdout, new RegExp(marker));
  assert.doesNotMatch(result.stderr, new RegExp(marker));
  assert.ok(calls[0].includes('s3://prsdb-seed-data-nft/performance-recovery/123/2/restore-state/nft-restore-state.json'));
});

test('private checkpoint uploads restore orchestration credentials after database verification', async () => {
  const workflow = await readFile(workflowPath, 'utf8');
  for (const job of workflow.split(/(?=^  [\w-]+:\s*$)/m)) {
    let role;
    for (const step of job.split(/(?=^      - (?:name|uses):)/m)) {
      if (step.includes('uses: ./.github/actions/verify-nft-database')) role = 'database';
      if (/uses: aws-actions\/configure-aws-credentials@/.test(step)) {
        role = step.includes(':role/nft-performance-test-orchestrator-access') ? 'orchestrator' : 'database';
      }
      if (/run: node scripts\/performance-tests\/nft-recovery-state\.mjs upload /.test(step)) {
        assert.equal(role, 'orchestrator', step.match(/- name: (.+)/)?.[1]);
      }
    }
  }
});

test('partial candidate worker ownership round-trips privately without publishing task metadata', async t => {
  const state = await setup(t);
  const workerState = {runId: '123-1', asyncRegistrationAttempted: true, candidateAsync: marker};
  await writeFile(join(state.directory, 'nft-restore-state.json'), JSON.stringify(workerState), {mode: 0o600});
  const upload = runRecovery(state, ['upload', 'worker-state']);
  const download = runRecovery(state, ['download'], {
    AWS_FAKE_STATE: JSON.stringify(workerState),
    AWS_FAKE_KEYS: JSON.stringify(['performance-recovery/123/2/worker-state/nft-restore-state.json']),
  });
  const select = runRecovery(state, ['select']);
  for (const result of [upload, download, select]) {
    assert.equal(result.status, 0);
    assert.doesNotMatch(result.stdout + result.stderr, new RegExp(marker));
  }
  assert.deepEqual(JSON.parse(await readFile(join(state.directory, 'nft-restore-state.json'), 'utf8')), workerState);
});

test('failed private checkpoint upload sanitizes AWS output and errors', async t => {
  const state = await setup(t);
  await writeFile(join(state.directory, 'nft-restore-state.json'),
    JSON.stringify({taskPrivateNetworkMetadata: marker}), {mode: 0o600});

  const result = runRecovery(state, ['upload', 'restore-state'], {AWS_FAKE_FAILURE: 'true'});

  assert.equal(result.status, 1);
  assert.doesNotMatch(result.stdout, new RegExp(marker));
  assert.doesNotMatch(result.stderr, new RegExp(marker));
  assert.match(result.stderr, /operator recovery may be required/);
});

test('failed private checkpoint retrieval sanitizes AWS output and errors', async t => {
  const state = await setup(t);

  const result = runRecovery(state, ['download'], {AWS_FAKE_FAILURE: 'true'});

  assert.equal(result.status, 1);
  assert.doesNotMatch(result.stdout, new RegExp(marker));
  assert.doesNotMatch(result.stderr, new RegExp(marker));
  assert.match(result.stderr, /operator recovery may be required/);
});

test('checkpoint storage cannot be redirected to another bucket', async t => {
  const state = await setup(t);
  await writeFile(join(state.directory, 'nft-restore-state.json'), '{}', {mode: 0o600});

  const result = runRecovery(state, ['upload', 'restore-state'], {NFT_RECOVERY_BUCKET: 'unapproved-bucket'});

  assert.equal(result.status, 1);
  assert.doesNotMatch(result.stdout, new RegExp(marker));
  assert.doesNotMatch(result.stderr, new RegExp(marker));
  await assert.rejects(readFile(state.log), {code: 'ENOENT'});
});

test('private retrieval selects only this run and validated prior attempts', async t => {
  const state = await setup(t);
  const keys = [
    'performance-recovery/123/1/restore-checkpoint/nft-restore-state.json',
    'performance-recovery/123/1/restore-state/nft-restore-state.json',
    'performance-recovery/123/1/readiness-state/nft-state.json',
    'performance-recovery/123/2/cleanup-state/nft-restore-state.json',
    'performance-recovery/123/2/cleanup-state/nft-state.json',
    'performance-recovery/123/3/restore-state/nft-restore-state.json',
    'performance-recovery/999/1/restore-state/nft-restore-state.json',
  ];

  const result = runRecovery(state, ['download'], {AWS_FAKE_KEYS: JSON.stringify(keys)});
  assert.equal(result.status, 0);
  assert.doesNotMatch(result.stdout, new RegExp(marker));
  assert.doesNotMatch(result.stderr, new RegExp(marker));

  const selection = runRecovery(state, ['select']);
  assert.equal(selection.status, 0);
  assert.doesNotMatch(selection.stdout, new RegExp(marker));
  assert.doesNotMatch(selection.stderr, new RegExp(marker));
  const restored = JSON.parse(await readFile(join(state.directory, 'nft-restore-state.json'), 'utf8'));
  assert.equal(restored.runId, '123-1');
  assert.equal(restored.readinessAttempted, true);

  const calls = (await readFile(state.log, 'utf8')).trim().split('\n').map(line => JSON.parse(line));
  assert.ok(calls.some(args => args[0] === 's3api' &&
    args.includes('prsdb-seed-data-nft') && args.includes('performance-recovery/123/')));
  assert.equal(calls.some(args => args.some(value => value.includes('/123/3/'))), false);
  assert.equal(calls.some(args => args.some(value => value.includes('/999/'))), false);
});

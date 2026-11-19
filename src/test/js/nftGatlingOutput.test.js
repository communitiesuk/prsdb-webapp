import assert from 'node:assert/strict';
import {chmod, mkdir, mkdtemp, readFile, readdir, rm, stat, writeFile} from 'node:fs/promises';
import {spawn, spawnSync} from 'node:child_process';
import {join} from 'node:path';
import {fileURLToPath} from 'node:url';
import {test} from 'node:test';

const workflowPath = new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url);
const helperPath = fileURLToPath(new URL('../../../scripts/performance-tests/nft-gatling.mjs', import.meta.url));
const marker = 'PRIVATE_GATLING_CANARY';
const stages = [
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
];

async function setup(t, {gradleExitCode = 0, gradleDelayMs = 0, readyFile} = {}) {
  const sessionFiles = process.env.TMPDIR;
  assert.ok(sessionFiles, 'TMPDIR must point to the private session workspace');
  const directory = await mkdtemp(join(sessionFiles, 'nft-gatling-output-'));
  await chmod(directory, 0o700);
  t.after(() => rm(directory, {recursive: true, force: true}));

  const bin = join(directory, 'bin');
  const runnerTemp = join(directory, 'runner-temp');
  const privateDirectory = join(directory, 'private-upload');
  const gradleArgs = join(directory, 'gradle-arguments.json');
  const awsCalls = join(directory, 'aws-calls.jsonl');
  await mkdir(bin, {mode: 0o700});
  await mkdir(privateDirectory, {mode: 0o700});

  const gradle = join(directory, 'gradlew');
  await writeFile(gradle, `#!/usr/bin/env node
const {appendFileSync, mkdirSync, symlinkSync, writeFileSync} = require('node:fs');
const {join} = require('node:path');
const {randomUUID} = require('node:crypto');
appendFileSync(process.env.FAKE_GRADLE_ARGS, JSON.stringify(process.argv.slice(2)) + '\\n');
process.stdout.write('${marker}_STDOUT https://private.test/path?token=private-token body=private-body');
process.stderr.write('${marker}_STDERR cookie=private-cookie');
const reportMode = process.env.FAKE_GATLING_REPORT_MODE ?? 'valid';
if (reportMode !== 'missing') {
  const stage = process.env.NFT_GATLING_STAGE ?? 'unknown';
  const report = join(process.cwd(), 'build', 'reports', 'gatling', stage + '-' + randomUUID());
  mkdirSync(join(report, 'assets'), {recursive: true});
  writeFileSync(join(report, 'index.html'), '${marker}_REPORT_' + stage);
  writeFileSync(join(report, 'assets', 'chart.js'), '${marker}_CHART_' + stage);
  writeFileSync(join(report, 'simulation.log'), '${marker}_EVENTS_' + stage);
  if (reportMode === 'symlink') symlinkSync(process.env.FAKE_SYMLINK_TARGET, join(report, 'assets', 'outside'));
}
if (process.env.FAKE_GRADLE_READY) writeFileSync(process.env.FAKE_GRADLE_READY, 'ready', {mode: 0o600});
const exitCode = Number(process.env.FAKE_GRADLE_EXIT_CODE ?? '${gradleExitCode}');
setTimeout(() => process.exit(exitCode), Number(process.env.FAKE_GRADLE_DELAY_MS ?? '${gradleDelayMs}'));
`);
  await chmod(gradle, 0o700);

  const aws = join(bin, 'aws');
  await writeFile(aws, `#!/usr/bin/env node
const {appendFileSync, copyFileSync, mkdirSync} = require('node:fs');
const {dirname, join} = require('node:path');
const args = process.argv.slice(2);
appendFileSync(process.env.FAKE_AWS_CALLS, JSON.stringify(args) + '\\n');
process.stderr.write('PRIVATE_AWS_CANARY');
if (process.env.FAKE_AWS_UPLOAD_FAILURE === 'true') process.exit(9);
  if (process.env.FAKE_AWS_REPORT_UPLOAD_FAILURE === 'true' && args[3]?.endsWith('/reports.tar.gz')) process.exit(9);
if (args[0] !== 's3' || args[1] !== 'cp') process.exit(8);
const destination = args[3];
const prefix = 's3://prsdb-seed-data-nft/';
if (!destination.startsWith(prefix)) process.exit(7);
const target = join(process.env.FAKE_PRIVATE_DIRECTORY, ...destination.slice(prefix.length).split('/'));
mkdirSync(dirname(target), {recursive: true, mode: 0o700});
copyFileSync(args[2], target);
`);
  await chmod(aws, 0o700);

  return {
    directory,
    runnerTemp,
    privateDirectory,
    gradleArgs,
    awsCalls,
    readyFile,
    env: {
      ...process.env,
      PATH: `${bin}:${process.env.PATH}`,
      RUNNER_TEMP: runnerTemp,
      GITHUB_RUN_ID: '123',
      GITHUB_RUN_ATTEMPT: '2',
      AWS_ACCESS_KEY_ID: 'test',
      AWS_SECRET_ACCESS_KEY: 'test',
      AWS_SESSION_TOKEN: 'test',
      AWS_EC2_METADATA_DISABLED: 'true',
      AWS_ENDPOINT_URL: 'http://127.0.0.1:9',
      AWS_REGION: 'eu-west-2',
      FAKE_GRADLE_ARGS: gradleArgs,
      FAKE_GRADLE_EXIT_CODE: String(gradleExitCode),
      FAKE_GRADLE_DELAY_MS: String(gradleDelayMs),
      FAKE_GATLING_REPORT_MODE: 'valid',
      FAKE_SYMLINK_TARGET: join(directory, 'outside-report-file'),
      FAKE_GRADLE_READY: readyFile ?? '',
      FAKE_AWS_CALLS: awsCalls,
      FAKE_PRIVATE_DIRECTORY: privateDirectory,
      FAKE_AWS_UPLOAD_FAILURE: 'false',
      FAKE_AWS_REPORT_UPLOAD_FAILURE: 'false',
    },
  };
}

function runHelper(state, stage = 'basicJourneys', extraArgs = [], env = state.env) {
  return spawnSync(process.execPath, [
    helperPath, stage, '--', './gradlew', 'gatlingRun', '--no-daemon', '--non-interactive',
    '--simulation', 'uk.gov.communities.prsdb.webapp.performance.SyntheticSimulation',
    '-Dgatling.destination=https://private.test/path?token=private-token',
    ...extraArgs,
  ], {cwd: state.directory, env, encoding: 'utf8'});
}

function publishHelper(state, env = state.env) {
  return spawnSync(process.execPath, [helperPath, 'publish'], {
    cwd: state.directory, env, encoding: 'utf8',
  });
}

async function privateObjects(state) {
  const files = [];
  async function visit(directory) {
    for (const entry of await readdir(directory, {withFileTypes: true})) {
      const path = join(directory, entry.name);
      if (entry.isDirectory()) await visit(path);
      else if (entry.name.endsWith('.stdout.log') || entry.name.endsWith('.stderr.log')) files.push(path);
    }
  }
  await visit(state.privateDirectory);
  return files;
}

test('all workflow Gatling launches use the fixed-stage output gate', async () => {
  const workflow = await readFile(workflowPath, 'utf8');
  const launches = [...workflow.matchAll(/node scripts\/performance-tests\/nft-gatling\.mjs ([A-Za-z]+) -- \.\/gradlew gatlingRun --no-daemon --non-interactive/g)]
    .map(match => match[1]);
  const directGradleLaunches = workflow.match(/^\s+\.\/gradlew gatlingRun --no-daemon --non-interactive/gm) ?? [];

  assert.deepEqual(launches, stages);
  assert.equal(directGradleLaunches.length, 0);
  const credentials = workflow.indexOf('- name: Refresh credentials for private recovery checkpoint');
  const publisher = workflow.indexOf('- name: Publish private Gatling diagnostics');
  const reportGuidance = workflow.indexOf('- name: Add private report download guidance');
  const readiness = workflow.indexOf('- name: Store readiness state privately');
  assert.ok(credentials >= 0 && credentials < publisher && publisher < reportGuidance && reportGuidance < readiness);
  assert.match(workflow.slice(publisher, readiness), /if: always\(\)/);
  assert.match(workflow.slice(reportGuidance, readiness), /aws s3 cp s3:\/\/prsdb-seed-data-nft\/performance-recovery/);
  assert.doesNotMatch(workflow.slice(reportGuidance, readiness), /X-Amz-Signature|https?:\/\/|PRIVATE_GATLING_CANARY/);
});

test('every workflow Gatling command runs through the gate without publishing canaries', async t => {
  const state = await setup(t);
  const workflow = await readFile(workflowPath, 'utf8');
  const lines = workflow.split(/\r?\n/);
  const commands = [];
  for (let index = 0; index < lines.length; index++) {
    if (!lines[index].trim().startsWith('node scripts/performance-tests/nft-gatling.mjs ')) continue;
    let command = lines[index].trim();
    while (command.endsWith('\\')) {
      index++;
      command = `${command.slice(0, -1)} ${lines[index].trim()}`;
    }
    commands.push(command.replace('node scripts/performance-tests/nft-gatling.mjs', `node "${helperPath}"`));
  }

  assert.equal(commands.length, stages.length);
  for (const command of commands) {
    const result = spawnSync('/bin/bash', ['-c', command], {
      cwd: state.directory,
      env: {...state.env, FAIL_GATLING: 'false', landlord_subject: 'synthetic'},
      encoding: 'utf8',
    });
    assert.equal(result.status, 0);
    assert.doesNotMatch(result.stdout + result.stderr, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  }
  assert.equal((await readFile(state.gradleArgs, 'utf8')).trim().split('\n').length, stages.length);
  const localFiles = await privateObjects({...state, privateDirectory: join(state.runnerTemp, 'nft-gatling-diagnostics', '123-2')});
  assert.equal(localFiles.length, stages.length * 2);
  const published = publishHelper(state);
  assert.equal(published.status, 0);
  assert.doesNotMatch(published.stdout + published.stderr, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  assert.match(published.stdout, /NFT_GATLING_DIAGNOSTICS status=complete elapsed_seconds=\d+ uploaded_file_count=39 exit_code=0 report_archive_status=complete/);
  assert.equal((await privateObjects(state)).length, stages.length * 2);
});

test('successful launch keeps canaries private and publishes only fixed progress', async t => {
  const state = await setup(t);
  const result = runHelper(state, 'rampedJourneys', ['-Dgatling.basic.iterations=1239']);
  const publicOutput = result.stdout + result.stderr;

  assert.equal(result.status, 0);
  assert.doesNotMatch(publicOutput, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  assert.match(publicOutput, /NFT_GATLING stage=rampedJourneys status=started elapsed_seconds=0/);
  assert.match(publicOutput, /NFT_GATLING stage=rampedJourneys status=complete elapsed_seconds=\d+ exit_code=0/);
  assert.deepEqual(JSON.parse(await readFile(state.gradleArgs, 'utf8')), [
    'gatlingRun', '--no-daemon', '--non-interactive', '--simulation',
    'uk.gov.communities.prsdb.webapp.performance.SyntheticSimulation',
    '-Dgatling.destination=https://private.test/path?token=private-token',
    '-Dgatling.basic.iterations=1239',
  ]);
  const localFiles = await privateObjects({...state, privateDirectory: join(state.runnerTemp, 'nft-gatling-diagnostics', '123-2')});
  assert.equal(localFiles.length, 2);
  for (const path of localFiles) assert.equal((await stat(path)).mode & 0o777, 0o600);
  assert.equal(await readFile(state.awsCalls, 'utf8').catch(error => error.code), 'ENOENT');

  const published = publishHelper(state);
  assert.equal(published.status, 0);
  assert.doesNotMatch(published.stdout + published.stderr, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  assert.match(published.stdout, /uploaded_file_count=3 exit_code=0 report_archive_status=complete/);
  const calls = (await readFile(state.awsCalls, 'utf8')).trim().split('\n').map(line => JSON.parse(line));
  assert.equal(calls.length, 3);
  const logCalls = calls.filter(args => args[3].includes('/gatling/rampedJourneys/'));
  assert.equal(logCalls.length, 2);
  assert.ok(logCalls.every(args => args[0] === 's3' && args[1] === 'cp' &&
    args[3].startsWith('s3://prsdb-seed-data-nft/performance-recovery/123/2/gatling/rampedJourneys/')));
  assert.ok(calls.some(args => args[3] === 's3://prsdb-seed-data-nft/performance-recovery/123/2/gatling/reports.tar.gz'));

  const objects = await privateObjects(state);
  assert.equal(objects.length, 2);
  const raw = (await Promise.all(objects.map(path => readFile(path, 'utf8')))).join('\n');
  assert.match(raw, /PRIVATE_GATLING_CANARY_STDOUT/);
  assert.match(raw, /PRIVATE_GATLING_CANARY_STDERR/);
  assert.match(raw, /private-token/);
  for (const path of objects) assert.equal((await stat(path)).mode & 0o777, 0o600);
});

test('Gradle failure propagates its exact exit code without exposing diagnostics', async t => {
  const state = await setup(t, {gradleExitCode: 23});
  const result = runHelper(state);

  assert.equal(result.status, 23);
  assert.doesNotMatch(result.stdout + result.stderr, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  assert.match(result.stdout, /status=failed elapsed_seconds=\d+ exit_code=23/);
  assert.equal((await privateObjects({...state, privateDirectory: join(state.runnerTemp, 'nft-gatling-diagnostics', '123-2')})).length, 2);
  assert.equal(publishHelper(state).status, 0);
  assert.equal((await privateObjects(state)).length, 2);
});

test('failed private diagnostic upload fails an otherwise successful launch safely', async t => {
  const state = await setup(t);
  const launch = runHelper(state);
  const result = publishHelper(state, {...state.env, FAKE_AWS_UPLOAD_FAILURE: 'true'});

  assert.equal(launch.status, 0);
  assert.equal(result.status, 1);
  assert.doesNotMatch(result.stdout + result.stderr, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  assert.match(result.stdout, /NFT_GATLING_DIAGNOSTICS status=failed elapsed_seconds=\d+ uploaded_file_count=0 exit_code=1/);
  assert.equal((await readFile(state.awsCalls, 'utf8')).trim().split('\n').length, 3);
});

test('aborted launch captures and uploads diagnostics before returning signal exit status', async t => {
  const sessionFiles = process.env.TMPDIR;
  assert.ok(sessionFiles, 'TMPDIR must point to the private session workspace');
  const readyFile = join(sessionFiles, `nft-gatling-abort-ready-${process.pid}`);
  t.after(() => rm(readyFile, {force: true}));
  const state = await setup(t, {gradleDelayMs: 30000, readyFile});
  const child = spawn(process.execPath, [
    helperPath, 'basicJourneys', '--', './gradlew', 'gatlingRun', '--simulation', 'SyntheticSimulation',
  ], {cwd: state.directory, env: state.env, stdio: ['ignore', 'pipe', 'pipe']});
  let stdout = '';
  let stderr = '';
  child.stdout.setEncoding('utf8').on('data', chunk => { stdout += chunk; });
  child.stderr.setEncoding('utf8').on('data', chunk => { stderr += chunk; });

  const deadline = Date.now() + 5000;
  while (Date.now() < deadline) {
    try {
      await readFile(readyFile);
      break;
    } catch {
      await new Promise(resolve => setTimeout(resolve, 10));
    }
  }
  await assert.doesNotReject(readFile(readyFile), 'fake Gradle process should have started');
  child.kill('SIGTERM');
  const exitCode = await new Promise(resolve => child.once('close', resolve));

  assert.equal(exitCode, 143);
  assert.doesNotMatch(stdout + stderr, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  assert.match(stdout, /status=aborted elapsed_seconds=\d+ exit_code=143/);
  const published = publishHelper(state);
  assert.equal(published.status, 0);
  assert.match(published.stdout, /report_archive_status=complete/);
  assert.doesNotMatch(published.stdout + published.stderr, /PRIVATE_|private-token|private-body|private-cookie|private\.test/);
  assert.equal((await privateObjects(state)).length, 2);
});

test('heartbeat output stays fixed and excludes raw process output', async t => {
  const state = await setup(t, {gradleDelayMs: 60});
  const {runGatlingStage} = await import(helperPath);
  const output = [];
  const result = await runGatlingStage({
    stage: 'basicJourneys',
    command: './gradlew',
    args: ['gatlingRun', '--simulation', 'SyntheticSimulation'],
    env: state.env,
    cwd: state.directory,
    heartbeatMs: 5,
    log: line => output.push(line),
  });

  assert.equal(result.exitCode, 0);
  assert.ok(output.some(line => /^NFT_GATLING stage=basicJourneys status=heartbeat elapsed_seconds=\d+$/.test(line)));
  assert.ok(output.every(line => !/PRIVATE_|private-token|private-body|private-cookie|private\.test/.test(line)));
});

test('unknown stages are rejected without launching Gradle or printing the supplied value', async t => {
  const state = await setup(t);
  const result = runHelper(state, 'private-stage-value');

  assert.equal(result.status, 1);
  assert.doesNotMatch(result.stdout + result.stderr, /private-stage-value|PRIVATE_|private-token/);
});

test('successful and failed Gradle stages retain their own HTML, assets, and raw events', async t => {
  const state = await setup(t);
  const successful = runHelper(state, 'basicJourneys');
  const failed = runHelper(state, 'rampedJourneys', [], {...state.env, FAKE_GRADLE_EXIT_CODE: '23'});

  assert.equal(successful.status, 0);
  assert.equal(failed.status, 23);
  assert.match(successful.stdout, /gradle_exit_code=0 report_status=complete exit_code=0/);
  assert.match(failed.stdout, /gradle_exit_code=23 report_status=complete exit_code=23/);
  assert.doesNotMatch(successful.stdout + successful.stderr + failed.stdout + failed.stderr,
    /PRIVATE_GATLING_CANARY|private-token|private-body|private-cookie|private\.test/);

  const reports = join(state.runnerTemp, 'nft-gatling-diagnostics', '123-2', 'reports');
  const stages = (await readdir(reports)).sort();
  assert.equal(stages.length, 2);
  const contents = await Promise.all(stages.map(async directory => {
    const reportDirectory = join(reports, directory);
    return Promise.all([
      readFile(join(reportDirectory, 'index.html'), 'utf8'),
      readFile(join(reportDirectory, 'assets', 'chart.js'), 'utf8'),
      readFile(join(reportDirectory, 'simulation.log'), 'utf8'),
    ]);
  }));
  assert.deepEqual(contents.map(files => files.map(file => file.match(/_(?:REPORT|CHART|EVENTS)_(\w+)/)?.[1])), [
    ['basicJourneys', 'basicJourneys', 'basicJourneys'],
    ['rampedJourneys', 'rampedJourneys', 'rampedJourneys'],
  ]);

  const published = publishHelper(state);
  assert.equal(published.status, 0);
  assert.doesNotMatch(published.stdout + published.stderr, /PRIVATE_GATLING_CANARY|private-token|private-body|private-cookie|private\.test/);
  assert.match(published.stdout, /report_archive_status=complete/);
  const archive = join(state.privateDirectory, 'performance-recovery', '123', '2', 'gatling', 'reports.tar.gz');
  const listing = spawnSync('tar', ['-tzf', archive], {encoding: 'utf8'});
  assert.equal(listing.status, 0);
  assert.match(listing.stdout, /reports\/basicJourneys\/index\.html/);
  assert.match(listing.stdout, /reports\/basicJourneys\/assets\/chart\.js/);
  assert.match(listing.stdout, /reports\/basicJourneys\/simulation\.log/);
  assert.match(listing.stdout, /reports\/rampedJourneys\/index\.html/);
  assert.match(listing.stdout, /reports\/rampedJourneys\/simulation\.log/);
});

test('report archive upload failure is distinct from successfully published stage diagnostics', async t => {
  const state = await setup(t);
  assert.equal(runHelper(state).status, 0);
  const result = publishHelper(state, {...state.env, FAKE_AWS_REPORT_UPLOAD_FAILURE: 'true'});

  assert.equal(result.status, 1);
  assert.match(result.stdout, /uploaded_file_count=2 exit_code=1 report_archive_status=failed/);
  assert.equal((await readFile(state.awsCalls, 'utf8')).trim().split('\n').length, 3);
  assert.equal((await privateObjects(state)).length, 2);
});

test('missing report fails explicitly while preserving the Gradle result', async t => {
  const state = await setup(t);
  const result = runHelper(state, 'basicJourneys', [], {...state.env, FAKE_GATLING_REPORT_MODE: 'missing'});

  assert.equal(result.status, 1);
  assert.match(result.stdout, /gradle_exit_code=0 report_status=missing exit_code=1/);
  assert.equal((await privateObjects({...state, privateDirectory: join(state.runnerTemp, 'nft-gatling-diagnostics', '123-2')})).length, 2);
});

test('report capture rejects symlinked report content without copying it', async t => {
  const state = await setup(t);
  const result = runHelper(state, 'basicJourneys', [], {...state.env, FAKE_GATLING_REPORT_MODE: 'symlink'});

  assert.equal(result.status, 1);
  assert.match(result.stdout, /gradle_exit_code=0 report_status=unsafe exit_code=1/);
  const reports = join(state.runnerTemp, 'nft-gatling-diagnostics', '123-2', 'reports');
  assert.equal(await readdir(reports).catch(error => error.code), 'ENOENT');
});

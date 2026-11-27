import assert from 'node:assert/strict';
import {spawnSync} from 'node:child_process';
import {readFile} from 'node:fs/promises';
import {test} from 'node:test';
import {journeyFixtures, uuidFor} from './nftJourneyFixtures.js';

test('combined workflow requires paid and council exports and preserves full-run-only success markers', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const ramp = workflow.split('id: rampedJourneys\n')[1].split('\n      - name:')[0];
  const summary = workflow.split('- name: Summarise journey outcomes\n')[1].split('\n      - name:')[0];
  const action = await readFile(new URL('../../../.github/actions/verify-nft-database/action.yml', import.meta.url), 'utf8');
  for (const [mode, id, variable] of [
    ['paid-registration', 'exportPaidRegistrationFixtures', 'EXPORT_PAID_REGISTRATION'],
    ['council-read', 'exportCouncilReadFixtures', 'EXPORT_COUNCIL_READ'],
  ]) {
    assert.ok(action.includes(`${mode}-fixtures`));
    assert.ok(workflow.includes(`mode: ${mode}-fixtures`));
    assert.ok(workflow.indexOf(`id: ${id}\n`) < workflow.indexOf('id: rampedJourneys\n'));
    assert.ok(ramp.includes(`steps.${id}.outcome == 'success'`));
    assert.ok(summary.includes(`${variable}: \${{ steps.${id}.outcome }}`));
  }
  const environment = Object.fromEntries([...summary.matchAll(/^\s+([A-Z_0-9]+): \$\{\{ steps\.[^.]+\.outcome }}/gm)]
    .map(([, name]) => [name, 'success']));
  environment.GITHUB_STEP_SUMMARY = '/dev/null';
  const shell = summary.split('        run: |\n')[1].split('\n').map(line => line.replace(/^          /, '')).join('\n');
  assert.equal(spawnSync('bash', ['-euo', 'pipefail', '-c', shell], {env: environment, encoding: 'utf8'}).status, 0);
  for (const variable of ['EXPORT_PAID_REGISTRATION', 'EXPORT_COUNCIL_READ', 'RAMPED_JOURNEYS']) {
    for (const outcome of ['failure', 'skipped', '']) {
      assert.notEqual(spawnSync('bash', ['-euo', 'pipefail', '-c', shell], {
        env: {...environment, [variable]: outcome}, encoding: 'utf8',
      }).status, 0, `${variable} ${outcome || 'missing'} must fail`);
    }
  }
  const markerCondition = workflow.split('\n  marker:\n')[1].split('    runs-on:')[0];
  assert.ok(markerCondition.includes("github.ref == 'refs/heads/nft'"));
  assert.ok(markerCondition.includes("needs.changes.outputs.run == 'true'"));
  assert.ok(!markerCondition.includes("== 'skipped'"));
});

test('concurrent preparation precedes ramp and correctness journeys use fresh exports', async () => {
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const position = name => {
    const found = workflow.indexOf(`id: ${name}\n`);
    assert.notEqual(found, -1, name);
    return found;
  };
  for (const id of ['exportRampTeam', 'exportRampAgentAccess', 'exportRampJointAcceptance', 'exportRampOffboarding']) {
    assert.ok(position(id) < position('rampedJourneys'));
    assert.ok(workflow.includes(`steps.${id}.outcome == 'success'`));
  }
  for (const id of ['exportAgentFixtures', 'exportJointFixtures', 'exportOffboardingFixtures']) {
    assert.ok(position('rampedJourneys') < position(id));
  }
  assert.match(workflow, /gatling\.ramp\.operatorFixtureOffset=11/);
  assert.match(workflow, /gatling\.ramp\.additionalPerMinute=1/);
});

test('workflow token matchers select current prepared invitations and reject mismatches', async () => {
  const {runInNewContext} = await import('node:vm');
  const workflow = await readFile(new URL('../../../.github/workflows/run-performance-tests.yml', import.meta.url), 'utf8');
  const scripts = [...workflow.matchAll(/node --input-type=module -[^\n]*<<'NODE'\n([\s\S]*?)\n          NODE/g)]
    .map(match => match[1].replace(/^ {10}/gm, '').replace(/^import .*;\n/gm, ''));
  assert.equal(scripts.length, 8);
  const team = journeyFixtures('organisation-team-ramp');
  const teamPrepared = team.map(({invitationToken, invitedEmail, ...row}) => row);
  const teamCurrent = team.map(row => ({...row, invitedEmail: `org-team-123-2-${row.organisationId}-kept@example.com`}));
  const agent = journeyFixtures('agent');
  const agentCurrent = agent.map(row => ({...row, token: uuidFor(row.propertyOwnershipId),
    invitedEmail: `agent-access-${row.propertyOwnershipId}@example.test`, hasPassword: false}));
  const joint = journeyFixtures('joint');
  const jointCurrent = joint.map(row => ({...row, token: uuidFor(row.propertyOwnershipId), invitationId: row.propertyOwnershipId}));
  const organisation = journeyFixtures('organisation', 39).map((_, i) => ({
    subject: `admin-${i}`, organisationId: i + 1, registrationNumber: i + 100, name: `Organisation ${i}`,
  }));
  const sequentialTeam = organisation.slice(22).map(row => ({...row, invitationToken: uuidFor(row.organisationId),
    invitedEmail: `org-team-123-2-${row.organisationId}-kept@example.com`}));
  const makeFiles = () => ({
    '/runner/nft-organisation-team-ramp-preparation-fixtures.json': teamPrepared,
    '/runner/nft-organisation-team-ramp-fixtures.json': teamCurrent,
    '/runner/nft-agent-fixtures.json': agent,
    '/runner/nft-agent-access-fixtures.json': agentCurrent,
    '/runner/nft-joint-fixtures.json': joint,
    '/runner/nft-joint-accept-fixtures.json': jointCurrent,
    '/runner/nft-offboarding-fixtures.json': journeyFixtures('offboarding'),
    '/runner/nft-organisation-fixtures.json': organisation,
    '/runner/nft-organisation-team-preparation-fixtures.json': organisation.slice(22),
    '/runner/nft-organisation-team-fixtures.json': sequentialTeam,
  });
  const execute = (index, files, ...paths) => {
    const writes = {};
    runInNewContext(scripts[index], {
      process: {argv: ['node', '-', ...paths], env: {RUNNER_TEMP: '/runner', GITHUB_RUN_ID: '123', GITHUB_RUN_ATTEMPT: '2'}},
      readFileSync: path => JSON.stringify(files[path]),
      writeFileSync: (path, data, options) => { assert.equal(options.mode, 0o600); writes[path] = JSON.parse(data); },
      join: (...parts) => parts.join('/'),
    });
    return writes;
  };
  const files = makeFiles();
  files['/runner/nft-agent-access-fixtures.json'] = [...agentCurrent].reverse();
  const writes = execute(0, files);
  assert.deepEqual(writes['/runner/nft-ramp-agent.json'], agentCurrent);
  assert.deepEqual(writes['/runner/nft-ramp-team.json'], teamCurrent);
  assert.deepEqual(writes['/runner/nft-ramp-joint.json'], jointCurrent);
  assert.deepEqual(execute(2, files, '/runner/nft-organisation-team-preparation-fixtures.json', '/runner/nft-organisation-team-fixtures.json')
    ['/runner/nft-organisation-team-fixtures.json'], sequentialTeam);
  assert.deepEqual(execute(4, files, '/runner/nft-agent-fixtures.json', '/runner/nft-agent-access-fixtures.json')
    ['/runner/nft-agent-access-fixtures.json'], agentCurrent);
  assert.deepEqual(execute(6, files, '/runner/nft-joint-fixtures.json', '/runner/nft-joint-accept-fixtures.json')
    ['/runner/nft-joint-accept-fixtures.json'], jointCurrent);
  const newTeam = teamPrepared.map(row => ({...row, subject: `fresh-${row.subject}`, organisationId: row.organisationId + 1000}));
  const teamPaths = ['/runner/team-preparation.json', '/runner/profiles.json', '/runner/team-ramp.json'];
  const teamInputs = {[teamPaths[0]]: newTeam, [teamPaths[1]]: organisation, [teamPaths[2]]: teamCurrent};
  execute(1, teamInputs, ...teamPaths);
  const overlap = structuredClone(teamInputs);
  overlap[teamPaths[0]][0].subject = teamCurrent[0].subject;
  assert.throws(() => execute(1, overlap, ...teamPaths), /actors overlap/);
  for (const [index, family, count, field] of [
    [3, 'agent', 28, 'ownerSubject'],
    [5, 'joint', 28, 'inviteeSubject'],
    [7, 'offboarding', 17, 'subject'],
  ]) {
    const candidates = journeyFixtures(family, count + 28);
    const ramp = candidates.slice(0, count);
    const paths = [`/runner/nft-${family}-fixtures.json`, `/runner/nft-ramp-${family}.json`];
    const input = {[paths[0]]: candidates, [paths[1]]: ramp};
    const selected = execute(index, input, ...paths)[paths[0]];
    assert.deepEqual(selected, candidates.slice(count));
    assert.ok(selected.every(row => !ramp.some(used => used[field] === row[field])));
    const insufficient = {[paths[0]]: candidates.slice(0, -1), [paths[1]]: ramp};
    assert.throws(() => execute(index, insufficient, ...paths), /Insufficient correctness actors/);
  }
  for (const [index, path, mutate, args] of [
    [0, '/runner/nft-organisation-team-ramp-fixtures.json', rows => { rows[0].invitedEmail = 'old-run@example.com'; }, []],
    [0, '/runner/nft-agent-access-fixtures.json', rows => rows.pop(), []],
    [0, '/runner/nft-joint-accept-fixtures.json', rows => { rows[0].invitedEmail = 'wrong@example.com'; }, []],
    [2, '/runner/nft-organisation-team-fixtures.json', rows => { rows[0].subject = 'wrong'; }, ['/runner/nft-organisation-team-preparation-fixtures.json', '/runner/nft-organisation-team-fixtures.json']],
    [4, '/runner/nft-agent-access-fixtures.json', rows => { rows[0].invitedEmail = 'wrong@example.com'; }, ['/runner/nft-agent-fixtures.json', '/runner/nft-agent-access-fixtures.json']],
    [6, '/runner/nft-joint-accept-fixtures.json', rows => rows.pop(), ['/runner/nft-joint-fixtures.json', '/runner/nft-joint-accept-fixtures.json']],
  ]) {
    const broken = structuredClone(makeFiles());
    mutate(broken[path]);
    assert.throws(() => execute(index, broken, ...args), /invitations? .*match/i);
  }
});

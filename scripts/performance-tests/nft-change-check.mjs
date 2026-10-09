import {execFile} from 'node:child_process';
import {promisify} from 'node:util';
import {mkdtemp, readFile, rm, appendFile} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {pathToFileURL} from 'node:url';

const execute = promisify(execFile);

export function shouldRunForNftHead({currentSha, previousSuccessfulSha = null, force = false}) {
  for (const sha of [currentSha, previousSuccessfulSha].filter(value => value !== null)) {
    if (!/^[a-f0-9]{40}$/.test(sha)) throw new Error('Expected a full Git commit SHA');
  }
  return force || currentSha !== previousSuccessfulSha;
}

export async function previousSuccessfulSha(gh, log = console.log) {
  const runs = JSON.parse(await gh([
    'run', 'list', '--repo', 'communitiesuk/prsdb-webapp', '--workflow', 'basic-performance-tests.yml',
    '--branch', 'main', '--status', 'success', '--limit', '1', '--json', 'databaseId',
  ]));
  if (!runs.length) {
    log('No successful readiness run exists; checking NFT readiness.');
    return null;
  }
  const {artifacts} = JSON.parse(await gh([
    'api', `repos/communitiesuk/prsdb-webapp/actions/runs/${runs[0].databaseId}/artifacts`,
  ]));
  const marker = artifacts.find(artifact => artifact.name === 'nft-head-sha' && !artifact.expired);
  if (!marker) {
    log('Previous success has no retained NFT SHA marker; checking NFT readiness.');
    return null;
  }
  const directory = await mkdtemp(join(tmpdir(), 'nft-head-sha-'));
  try {
    await gh(['run', 'download', String(runs[0].databaseId), '--repo', 'communitiesuk/prsdb-webapp',
      '--name', 'nft-head-sha', '--dir', directory]);
    return (await readFile(join(directory, 'nft-head-sha.txt'), 'utf8')).trim();
  } finally {
    await rm(directory, {recursive: true, force: true});
  }
}

// Wraps git/gh failures the same way AWS failures are wrapped: report the command and exit
// status only, never raw stdout/stderr, which can otherwise echo repository or token details.
async function runCommand(command, args) {
  try {
    return (await execute(command, args, {timeout: 60000})).stdout;
  } catch (error) {
    throw new Error(`${command} ${args[0] ?? ''} failed (exit ${error.code ?? 'unknown'})`);
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const gh = args => runCommand('gh', args);
  try {
    const currentSha = (await runCommand('git', ['-C', 'nft-source', 'rev-parse', 'HEAD'])).trim();
    const previousSha = await previousSuccessfulSha(gh);
    const run = shouldRunForNftHead({currentSha, previousSuccessfulSha: previousSha,
      force: process.env.FORCE_READINESS === 'true'});
    console.log(run ? 'NFT readiness required.' : 'NFT unchanged; skipping AWS operations.');
    await appendFile(process.env.GITHUB_OUTPUT, `run=${run}\nsha=${currentSha}\n`);
  } catch (error) {
    // Detail is already sanitized above; keep it off console.error/warn/trace so caught error
    // details are never written to those streams (CWE-209), while still logging the summary.
    console.log(`NFT change check details: ${error.message}`);
    console.error('NFT change check failed.');
    process.exitCode = 1;
  }
}

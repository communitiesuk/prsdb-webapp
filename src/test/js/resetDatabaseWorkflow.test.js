import assert from 'node:assert/strict';
import {execFile} from 'node:child_process';
import {mkdtemp, mkdir, readFile, rm, writeFile} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {promisify} from 'node:util';
import test from 'node:test';

const execute = promisify(execFile);
const workflows = [
  ['build and deploy', await readFile(new URL('../../../.github/workflows/build-and-deploy.yml', import.meta.url), 'utf8')],
  ['reset database', await readFile(new URL('../../../.github/workflows/reset-database.yml', import.meta.url), 'utf8')],
];
const sessionMarker = 'synthetic-private-reset-session';

test('database reset does not install JDK 25 for Flyway-only tasks', () => {
  const reset = workflows.find(([name]) => name === 'reset database')[1];
  assert.doesNotMatch(reset, /Set up Java for NFT reset/);
});

for (const [workflowName, workflow] of workflows) {
  const sessionStep = workflow.match(
    /      - name: Start SSM port-forwarding Session[\s\S]*?        run: \|\n([\s\S]*?)(?=\n      - name:)/,
  )?.[1];
  assert(sessionStep, `The ${workflowName} SSM step must remain present`);

  for (const environment of ['nft', 'integration', 'test']) {
    test(`${workflowName} ${environment} SSM tunnel keeps session output private`, async () => {
    const directory = await mkdtemp(join(tmpdir(), 'reset-workflow-'));
    try {
      const bin = join(directory, 'bin');
      await mkdir(bin, {mode: 0o700});
      await writeFile(join(bin, 'aws'), `#!/bin/sh
case "$1 $2" in
  "ec2 describe-instances") echo i-synthetic-reset ;;
  "ssm get-parameter") echo synthetic-private-database:5432 ;;
  "ssm start-session")
    if [ "$FAKE_SSM_FAILURE" = "true" ]; then
      echo "An error occurred (AccessDeniedException) when calling the StartSession operation: sensitive-detail" >&2
      exit 255
    fi
    echo "Starting session with SessionId: ${sessionMarker}"
    ;;
  *) exit 89 ;;
esac
`, {mode: 0o700});
      await writeFile(join(bin, 'sleep'), '#!/bin/sh\n/bin/sleep 0.1\n', {mode: 0o700});
      // Reproduce the workflow's GNU grep extraction on macOS, where grep has no -P option.
      await writeFile(join(bin, 'grep'), `#!${process.execPath}
const fs = require('node:fs');
if (process.argv[2] !== '-oP' || !process.argv[3].startsWith('SessionId: ')) process.exit(89);
const matches = [...fs.readFileSync(process.argv[4], 'utf8').matchAll(/SessionId: ([a-zA-Z0-9-]+)/g)];
for (const match of matches) console.log(match[1]);
if (!matches.length) process.exitCode = 1;
`, {mode: 0o700});
      const script = sessionStep.replace(/^          /gm, '')
        .replaceAll('${{ inputs.environment }}', environment);
      const scriptPath = join(directory, 'start-session.sh');
      const outputPath = join(directory, 'github-output');
      await writeFile(scriptPath, script, {mode: 0o600});
      const {stdout, stderr} = await execute('bash', ['-e', scriptPath], {cwd: directory, env: {
        ...process.env,
        PATH: `${bin}:${process.env.PATH}`,
        RUNNER_TEMP: directory,
        GITHUB_OUTPUT: outputPath,
        AWS_REGION: 'eu-west-2',
        AWS_ACCESS_KEY_ID: 'test',
        AWS_SECRET_ACCESS_KEY: 'test',
        AWS_EC2_METADATA_DISABLED: 'true',
        AWS_ENDPOINT_URL: 'http://127.0.0.1:9',
      }});
      assert.equal(stderr, '');
      const publicOutput = stdout.split('\n').filter(line => !line.startsWith('::add-mask::')).join('\n');
      assert.equal(publicOutput.includes(sessionMarker), false);
      assert.match(await readFile(outputPath, 'utf8'), new RegExp(`^SSM_SESSION_ID=${sessionMarker}\\n$`));
      assert(stdout.includes(`::add-mask::${sessionMarker}`));
      assert.match(script, /mktemp "\$RUNNER_TEMP\/ssm-port-forward\./);
      assert.match(script, /trap .*SSM_OUTPUT_FILE/);
      assert.doesNotMatch(script, /cat "\$SSM_OUTPUT_FILE"/);
    } finally {
      await rm(directory, {recursive: true, force: true});
    }
    });

    test(`${workflowName} ${environment} SSM failures report a safe actionable error`, async () => {
      const directory = await mkdtemp(join(tmpdir(), 'reset-workflow-failure-'));
      try {
        const bin = join(directory, 'bin');
        await mkdir(bin, {mode: 0o700});
        await writeFile(join(bin, 'aws'), `#!/bin/sh
case "$1 $2" in
  "ec2 describe-instances") echo i-synthetic-reset ;;
  "ssm get-parameter") echo synthetic-private-database:5432 ;;
  "ssm start-session")
    echo "An error occurred (AccessDeniedException) when calling the StartSession operation: sensitive-detail" >&2
    exit 255
    ;;
  *) exit 89 ;;
esac
`, {mode: 0o700});
        await writeFile(join(bin, 'sleep'), '#!/bin/sh\n/bin/sleep 0.1\n', {mode: 0o700});
        const script = sessionStep.replace(/^          /gm, '')
          .replaceAll('${{ inputs.environment }}', environment);
        const scriptPath = join(directory, 'start-session.sh');
        await writeFile(scriptPath, script, {mode: 0o600});
        await assert.rejects(execute('bash', ['-e', scriptPath], {cwd: directory, env: {
          ...process.env,
          PATH: `${bin}:${process.env.PATH}`,
          RUNNER_TEMP: directory,
          GITHUB_OUTPUT: join(directory, 'github-output'),
          AWS_REGION: 'eu-west-2',
          AWS_ACCESS_KEY_ID: 'test',
          AWS_SECRET_ACCESS_KEY: 'test',
          AWS_EC2_METADATA_DISABLED: 'true',
          AWS_ENDPOINT_URL: 'http://127.0.0.1:9',
        }}), error => {
          assert.match(error.stdout, /AccessDeniedException/);
          assert.match(error.stdout, /permissions/);
          assert.doesNotMatch(error.stdout, /sensitive-detail|SessionId:|session-token/i);
          return true;
        });
      } finally {
        await rm(directory, {recursive: true, force: true});
      }
    });
  }
}

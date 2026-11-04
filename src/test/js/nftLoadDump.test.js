import assert from 'node:assert/strict';
import {writeFile} from 'node:fs/promises';
import {test} from 'node:test';
import {loadSeededDataset, restoreDatabase} from '../../../scripts/performance-tests/nft-load-dump.mjs';

const config = {
  target: 'nft',
  account: '448120078528',
  bucket: 'prsdb-seed-data-nft',
  key: 'seed-data.dump',
};

function dependencies(overrides = {}) {
  const calls = [];
  return {
    calls,
    aws: async (service, operation, input = {}) => {
      calls.push(['aws', service, operation, input]);
      if (service === 'sts') return {Account: config.account};
      if (service === 's3api' && operation === 'head-object') {
        return {ContentLength: 1024, ETag: '"dump-etag"', VersionId: 'baseline-version'};
      }
      return {};
    },
    download: async input => {
      calls.push(['download', input]);
      await writeFile(input.destinationPath, Buffer.alloc(1024));
      return input.destinationPath;
    },
    inspectArchive: async path => calls.push(['inspect', path]),
    restoreDatabase: async path => calls.push(['restore', path]),
    migrateDatabase: async () => calls.push(['migrate']),
    verifyDatabase: async (environment, requirements) => calls.push(['verify', requirements]),
    env: {
      FLYWAY_URL: 'jdbc:postgresql://localhost:5432/prsdb',
      FLYWAY_USER: 'test-user',
      FLYWAY_PASSWORD: 'test-password',
    },
    log: () => {},
    ...overrides,
  };
}

test('accepts omitted dependency overrides when invoked by the CLI', async () => {
  await assert.rejects(loadSeededDataset({...config, target: 'test'}), /NFT only/);
});

test('restores with four workers after clearing the schema and preserves fail-fast flags', async () => {
  const calls = [];
  const execute = async (...args) => {
    calls.push(args);
    return {stdout: ''};
  };
  const environment = {PGPASSWORD: 'test-password', PGDATABASE: 'prsdb'};

  await restoreDatabase('/tmp/seed-data.dump', environment, execute);

  assert.equal(calls.length, 2);
  assert.equal(calls[0][0], 'docker');
  assert.ok(calls[0][1].includes('psql'));
  assert.ok(calls[0][1].includes('DROP SCHEMA public CASCADE; CREATE SCHEMA public AUTHORIZATION CURRENT_USER;'));
  assert.equal(calls[1][0], 'docker');
  assert.ok(calls[1][1].includes('pg_restore'));
  for (const flag of ['--jobs=4', '--exit-on-error', '--no-owner', '--no-acl']) {
    assert.ok(calls[1][1].includes(flag), `Missing restore flag ${flag}`);
  }
  assert.equal(calls[1][2].env.PGPASSWORD, environment.PGPASSWORD);
  assert.equal(calls[1][1].includes(environment.PGPASSWORD), false);
});

test('reports timed phases and a heartbeat without exposing restore error details', async t => {
  t.mock.timers.enable({apis: ['Date', 'setInterval'], now: 0});
  const logs = [];
  let markStarted;
  const started = new Promise(resolve => { markStarted = resolve; });
  let failRestore;
  const restoration = new Promise((resolve, reject) => { failRestore = reject; });
  const deps = dependencies({
    log: message => logs.push(message),
    restoreDatabase: async () => {
      markStarted();
      await restoration;
    },
  });
  const loading = loadSeededDataset(config, deps);
  const rejected = assert.rejects(loading, /sensitive restore details/);
  await started;
  t.mock.timers.tick(60000);
  failRestore(new Error('sensitive restore details'));
  await rejected;

  const phases = logs.filter(message => message.includes(': started'));
  assert.deepEqual(phases.map(message => message.replace(/^.*Z /, '')), [
    'Download seed dump: started',
    'Inspect PostgreSQL archive: started',
    'Restore database (4 workers): started',
  ]);
  assert.ok(logs.some(message => /Z Download seed dump: completed in \d+\.\ds$/.test(message)));
  assert.ok(logs.some(message => /Z Restore database \(4 workers\): still running \(60\.0s elapsed\)$/.test(message)));
  assert.ok(logs.some(message => /Z Restore database \(4 workers\): failed after 60\.0s$/.test(message)));
  assert.equal(logs.some(message => message.includes('sensitive restore details')), false);
  const logCount = logs.length;
  t.mock.timers.tick(60000);
  assert.equal(logs.length, logCount);
  assert.equal(deps.calls.some(([kind]) => kind === 'migrate'), false);
});

test('rejects non-NFT targets before making AWS or database calls', async () => {
  const deps = dependencies();

  await assert.rejects(loadSeededDataset({...config, target: 'test'}, deps), /NFT only/);
  assert.deepEqual(deps.calls, []);
});

test('rejects a caller in the wrong AWS account before inspecting the dump', async () => {
  const deps = dependencies({
    aws: async (...args) => {
      deps.calls.push(['aws', ...args]);
      return {Account: '000000000000'};
    },
  });

  await assert.rejects(loadSeededDataset(config, deps), /Wrong NFT AWS account/);
  assert.deepEqual(deps.calls, [['aws', 'sts', 'get-caller-identity']]);
});

test('refuses an empty S3 object without stopping to restore the database', async () => {
  const deps = dependencies({
    aws: async (service, operation, input = {}) => {
      deps.calls.push(['aws', service, operation, input]);
      if (service === 'sts') return {Account: config.account};
      return {ContentLength: 0};
    },
  });

  await assert.rejects(loadSeededDataset(config, deps), /empty or missing/);
  assert.equal(deps.calls.some(([kind]) => kind === 'restore'), false);
});

test('rejects insufficient disk space before downloading or restoring the database', async () => {
  const deps = dependencies({
    fileStatFs: async () => ({bavail: 1, bsize: 1}),
  });

  await assert.rejects(loadSeededDataset(config, deps), /Insufficient runner disk space/);
  assert.equal(deps.calls.some(([kind]) => kind === 'download'), false);
  assert.equal(deps.calls.some(([kind]) => kind === 'restore'), false);
});

test('downloads the selected immutable dump version, verifies it, restores and migrates in order', async () => {
  const logs = [];
  const deps = dependencies({log: message => logs.push(message)});

  const result = await loadSeededDataset(config, deps);

  assert.equal(result.object.versionId, 'baseline-version');
  assert.equal(result.object.etag, '"dump-etag"');
  assert.deepEqual(deps.calls.map(([kind]) => kind), [
    'aws',
    'aws',
    'download',
    'inspect',
    'restore',
    'migrate',
    'verify',
  ]);
  assert.equal(deps.calls[2][0], 'download');
  assert.deepEqual(
    Object.fromEntries(Object.entries(deps.calls[2][1]).filter(([name]) => name !== 'destinationPath')),
    {
      bucket: config.bucket,
      key: config.key,
      versionId: 'baseline-version',
      ifMatch: undefined,
    },
  );
  assert.match(deps.calls[2][1].destinationPath, /nft-seed-dump-.*seed-data\.dump$/);
  assert.deepEqual(
    deps.calls.filter(([kind]) => kind === 'aws').map(([, service, operation]) => [service, operation]),
    [['sts', 'get-caller-identity'], ['s3api', 'head-object']],
  );
  assert.deepEqual(deps.calls.find(([kind]) => kind === 'verify')[1], {
    minimumCompleted: 2450000,
    minimumIncomplete: 272223,
    minimumFreeAddresses: 1,
  });
  assert.deepEqual(logs.filter(message => message.includes(': started'))
    .map(message => message.replace(/^.*Z /, '')), [
    'Download seed dump: started',
    'Inspect PostgreSQL archive: started',
    'Restore database (4 workers): started',
    'Apply Flyway migrations: started',
    'Verify dataset and analyze: started',
  ]);
  assert.equal(logs.filter(message => /: completed in \d+\.\ds$/.test(message)).length, 5);
});

test('uses configured minimum dataset counts when verifying the restored dump', async () => {
  const deps = dependencies({
    env: {
      FLYWAY_URL: 'jdbc:postgresql://localhost:5432/prsdb',
      FLYWAY_USER: 'test-user',
      FLYWAY_PASSWORD: 'test-password',
      NFT_SEED_PROPERTIES: '100',
      NFT_SEED_INCOMPLETE_PROPERTIES: '20',
      NFT_LOAD_MINIMUM_FREE_ADDRESSES: '7',
    },
  });

  await loadSeededDataset(config, deps);

  assert.deepEqual(deps.calls.find(([kind]) => kind === 'verify')[1], {
    minimumCompleted: 100,
    minimumIncomplete: 20,
    minimumFreeAddresses: 7,
  });
});

test('rejects invalid dataset count overrides before contacting AWS', async () => {
  const deps = dependencies({
    env: {
      FLYWAY_URL: 'jdbc:postgresql://localhost:5432/prsdb',
      FLYWAY_USER: 'test-user',
      FLYWAY_PASSWORD: 'test-password',
      NFT_SEED_PROPERTIES: '-1',
    },
  });

  await assert.rejects(loadSeededDataset(config, deps), /NFT_SEED_PROPERTIES must be a non-negative integer/);
  assert.deepEqual(deps.calls, []);
});

test('rejects a partial download before changing the database', async () => {
  const deps = dependencies({
    fileStat: async () => ({size: 512}),
  });

  await assert.rejects(loadSeededDataset(config, deps), /size does not match S3 metadata/);
  assert.equal(deps.calls.some(([kind]) => kind === 'inspect'), false);
  assert.equal(deps.calls.some(([kind]) => kind === 'restore'), false);
});

test('rejects an invalid PostgreSQL archive before replacing the database', async () => {
  const deps = dependencies({
    inspectArchive: async path => {
      deps.calls.push(['inspect', path]);
      throw new Error('invalid archive');
    },
  });

  await assert.rejects(loadSeededDataset(config, deps), /invalid archive/);
  assert.equal(deps.calls.some(([kind]) => kind === 'restore'), false);
  assert.equal(deps.calls.some(([kind]) => kind === 'migrate'), false);
});

test('uses an ETag condition when the bucket has no version ID', async () => {
  const deps = dependencies({
    aws: async (service, operation, input = {}) => {
      deps.calls.push(['aws', service, operation, input]);
      if (service === 'sts') return {Account: config.account};
      return {ContentLength: 1024, ETag: '"dump-etag"'};
    },
  });

  await loadSeededDataset(config, deps);

  assert.equal(deps.calls[2][0], 'download');
  assert.deepEqual(
    Object.fromEntries(Object.entries(deps.calls[2][1]).filter(([name]) => name !== 'destinationPath')),
    {
      bucket: config.bucket,
      key: config.key,
      versionId: undefined,
      ifMatch: '"dump-etag"',
    },
  );
});

test('does not migrate or verify when database restoration fails', async () => {
  const deps = dependencies({
    restoreDatabase: async path => {
      deps.calls.push(['restore', path]);
      throw new Error('restore failed');
    },
  });

  await assert.rejects(loadSeededDataset(config, deps), /restore failed/);
  assert.equal(deps.calls.some(([kind]) => kind === 'migrate'), false);
  assert.equal(deps.calls.some(([kind]) => kind === 'verify'), false);
});

test('does not report success when migration fails', async () => {
  const deps = dependencies({
    migrateDatabase: async () => {
      deps.calls.push(['migrate']);
      throw new Error('migration failed');
    },
  });

  await assert.rejects(loadSeededDataset(config, deps), /migration failed/);
  assert.equal(deps.calls.some(([kind]) => kind === 'verify'), false);
});
